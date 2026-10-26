# Network Config Change Automator

A Java / Spring Boot tool that lets an operations team roll out configuration changes to network
devices (switches, routers) safely: with approval, a dry run, a staged rollout, automatic rollback
and a full audit log.

Network teams still make many changes by hand, and one bad change can take a site offline.
This tool puts the same controls around a change that a production change-management process requires.

## How a change moves through the system

1. **Request**: an engineer submits a change (config lines, target device group, reason). Status: `PENDING_APPROVAL`.
2. **Approval**: a *different* person must approve it. The requester cannot approve their own change.
3. **Dry run**: shows exactly which lines would be added or removed on each device, and runs the pre-check (minimum OS version). Nothing is changed.
4. **Staged deploy**: one **canary** device first, then batches. If any device fails or is unhealthy after the change, the rollout stops, the remaining devices are skipped, and devices already changed are restored automatically.
5. **Rollback**: a deployed change can be reverted on every device with one call.
6. **Audit log**: who created, approved, deployed and rolled back each change, with timestamps.

```
PENDING_APPROVAL --approve--> APPROVED --deploy--> DEPLOYED --rollback--> ROLLED_BACK
        |                          |
     reject                  failure / pre-check
        v                          v
    REJECTED                    FAILED (devices restored)
```

## Config line rules

- `ntp server 10.10.0.200` adds the line if it is not already there.
- `no snmp-server community public RO` removes that line if it is there.
- Blank lines and `!` comments are ignored. Applying the same change twice is safe.

## Run it

Needs Java 21 and Maven.

```bash
mvn spring-boot:run          # in-memory H2 database, sample network loaded automatically
mvn verify                   # run the tests
```

With PostgreSQL and Docker:

```bash
docker compose up --build    # app on http://localhost:8080, PostgreSQL in a container
```

The sample network has three device groups:

| Group | Devices | Use it to demo |
|---|---|---|
| `core-routers` | 2 healthy routers | a clean successful rollout |
| `campus-switches` | 4 switches, one on old OS `16.12.5` | the OS pre-check blocking a change |
| `lab-switches` | 4 switches, the 3rd rejects changes | automatic rollback after a failure |

## Try it (the acting user is sent in the `X-User` header)

```bash
# 1. alice requests a change for the lab switches (batch size 2)
curl -s -X POST localhost:8080/api/changes -H 'X-User: alice' -H 'Content-Type: application/json' -d '{
  "title":"Replace SNMP community and NTP server","reason":"Security audit",
  "targetGroup":"lab-switches",
  "configLines":"no snmp-server community public RO\nntp server 10.10.0.200",
  "batchSize":2}'

# 2. alice cannot approve it herself (403), bob can
curl -s -X POST localhost:8080/api/changes/1/approve -H 'X-User: alice'
curl -s -X POST localhost:8080/api/changes/1/approve -H 'X-User: bob'

# 3. preview the change on every device
curl -s localhost:8080/api/changes/1/dry-run

# 4. deploy: sw-lab-03 fails, so the rollout stops and sw-lab-01/02 are restored
curl -s -X POST localhost:8080/api/changes/1/deploy -H 'X-User: alice'

# 5. read the audit trail
curl -s localhost:8080/api/changes/1/audit
```

Repeat with `"targetGroup":"core-routers"` for a clean deploy followed by
`POST /api/changes/{id}/rollback`, or with `"minOsVersion":"17.3"` on `campus-switches` to see the pre-check block it.

## API

| Method | Path | Purpose |
|---|---|---|
| POST / GET | `/api/devices`, `/api/devices?group=` | register and list devices |
| POST | `/api/changes` | create a change (`X-User` = requester) |
| GET | `/api/changes`, `/api/changes/{id}` | list changes; one change with per-device results |
| POST | `/api/changes/{id}/approve` · `/reject` | approval step |
| GET | `/api/changes/{id}/dry-run` | preview and pre-check |
| POST | `/api/changes/{id}/deploy` | staged rollout |
| POST | `/api/changes/{id}/rollback` | revert a deployed change |
| GET | `/api/changes/{id}/audit`, `/api/audit` | audit trail |

## Design notes

- `ChangeService` holds the workflow rules; controllers only translate HTTP.
- `DeviceClient` is the boundary to real hardware. `SimulatedDeviceClient` keeps each device's
  "running config" in the database and can simulate failures (`faultMode`: `APPLY_FAILS`,
  `UNHEALTHY_AFTER_CHANGE`). A real implementation would talk SSH or NETCONF to IOS XE devices
  without changing the workflow code.
- Each device's previous config is stored before it is changed, which is what makes both automatic and manual rollback possible.
- Business-rule failures (own-approval, wrong status, pre-check) are audited even though the request is rejected.
- Not included yet: real authentication (the `X-User` header is a stand-in), a real SSH/NETCONF client, a web UI.

## Tests

`mvn verify` runs unit tests for the diff and version logic, workflow tests (approval rules, canary and
batch order, failure and rollback, pre-check, audit order) and a REST end-to-end test. CI is in `.github/workflows/ci.yml`.
