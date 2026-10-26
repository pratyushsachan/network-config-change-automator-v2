package com.netchange.service;

import com.netchange.device.DeviceClient;
import com.netchange.device.DeviceException;
import com.netchange.domain.ChangeRequest;
import com.netchange.domain.ChangeStatus;
import com.netchange.domain.Device;
import com.netchange.domain.DeviceExecution;
import com.netchange.domain.ExecutionStatus;
import com.netchange.errors.NetChangeException;
import com.netchange.repo.ChangeRequestRepository;
import com.netchange.repo.DeviceExecutionRepository;
import com.netchange.repo.DeviceRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The change-management workflow:
 * create (pending approval) -> approve by a second person -> dry run -> staged deploy
 * (canary first, then batches) -> automatic rollback on failure, or manual rollback later.
 * Every step is written to the audit log.
 */
@Service
public class ChangeService {

    private static final int DEFAULT_BATCH_SIZE = 2;

    private final ChangeRequestRepository changes;
    private final DeviceRepository devices;
    private final DeviceExecutionRepository executions;
    private final DeviceClient client;
    private final AuditService audit;

    public ChangeService(ChangeRequestRepository changes, DeviceRepository devices,
                         DeviceExecutionRepository executions, DeviceClient client, AuditService audit) {
        this.changes = changes;
        this.devices = devices;
        this.executions = executions;
        this.client = client;
        this.audit = audit;
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public List<ChangeRequest> list() {
        return changes.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public ChangeRequest get(Long id) {
        return changes.findById(id)
                .orElseThrow(() -> NetChangeException.notFound("Change " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<DeviceExecution> executionsFor(Long id) {
        get(id);
        return executions.findByChangeIdOrderByStepOrderAsc(id);
    }

    // --------------------------------------------------------------- workflow

    @Transactional
    public ChangeRequest create(String title, String reason, String targetGroup, String configLines,
                                String minOsVersion, Integer batchSize, String user) {
        if (devices.findByDeviceGroupOrderByNameAsc(targetGroup).isEmpty()) {
            throw NetChangeException.conflict("No devices in group '" + targetGroup + "'");
        }
        int size = batchSize == null ? DEFAULT_BATCH_SIZE : batchSize;
        ChangeRequest change = changes.save(
                new ChangeRequest(title, reason, targetGroup, configLines, minOsVersion, size, user));
        audit.record(change.getId(), user, "CREATED",
                "Change '" + title + "' for group " + targetGroup + ", batch size " + size);
        return change;
    }

    @Transactional(noRollbackFor = NetChangeException.class)
    public ChangeRequest approve(Long id, String user) {
        ChangeRequest change = get(id);
        requireStatus(change, ChangeStatus.PENDING_APPROVAL, "approved");
        if (change.getRequestedBy().equalsIgnoreCase(user)) {
            audit.record(id, user, "APPROVAL_DENIED", "Requester tried to approve their own change");
            throw NetChangeException.forbidden("The requester cannot approve their own change");
        }
        change.setApprovedBy(user);
        change.setStatus(ChangeStatus.APPROVED);
        audit.record(id, user, "APPROVED", "Change approved");
        return change;
    }

    @Transactional
    public ChangeRequest reject(Long id, String user, String reason) {
        ChangeRequest change = get(id);
        requireStatus(change, ChangeStatus.PENDING_APPROVAL, "rejected");
        change.setStatus(ChangeStatus.REJECTED);
        audit.record(id, user, "REJECTED", reason == null ? "Change rejected" : reason);
        return change;
    }

    @Transactional(readOnly = true)
    public DryRunResult dryRun(Long id) {
        ChangeRequest change = get(id);
        List<Device> targets = targetDevices(change);
        List<String> problems = precheck(change, targets);
        List<DryRunResult.DeviceDiff> diffs = new ArrayList<>();
        List<List<Device>> stages = stages(targets, change.getBatchSize());
        for (int s = 0; s < stages.size(); s++) {
            for (Device d : stages.get(s)) {
                ConfigDiff.Result diff = ConfigDiff.diff(client.getRunningConfig(d), change.getConfigLines());
                diffs.add(new DryRunResult.DeviceDiff(d.getName(), d.getOsVersion(), stageName(s),
                        isCompatible(change, d), diff.added(), diff.removed()));
            }
        }
        return new DryRunResult(id, problems.isEmpty(), problems, diffs);
    }

    @Transactional(noRollbackFor = NetChangeException.class)
    public ChangeRequest deploy(Long id, String user) {
        ChangeRequest change = get(id);
        requireStatus(change, ChangeStatus.APPROVED, "deployed");

        List<Device> targets = targetDevices(change);
        List<String> problems = precheck(change, targets);
        if (!problems.isEmpty()) {
            audit.record(id, user, "DEPLOY_BLOCKED", String.join("; ", problems));
            throw NetChangeException.conflict("Pre-check failed: " + String.join("; ", problems));
        }

        audit.record(id, user, "DEPLOY_STARTED", targets.size() + " device(s) in group " + change.getTargetGroup());

        record Applied(Device device, DeviceExecution execution) {
        }
        List<Applied> applied = new ArrayList<>();
        List<List<Device>> stages = stages(targets, change.getBatchSize());
        boolean failed = false;
        int step = 0;

        for (int s = 0; s < stages.size(); s++) {
            for (Device device : stages.get(s)) {
                DeviceExecution ex = new DeviceExecution(id, device, ++step, stageName(s));
                if (failed) {
                    ex.mark(ExecutionStatus.SKIPPED, "Skipped because an earlier device failed");
                    executions.save(ex);
                    continue;
                }

                String before = client.getRunningConfig(device);
                String after = ConfigDiff.apply(before, change.getConfigLines());
                ex.setPreviousConfig(before);
                ex.setNewConfig(after);

                try {
                    client.applyConfig(device, after);
                } catch (DeviceException e) {
                    ex.mark(ExecutionStatus.FAILED, e.getMessage());
                    executions.save(ex);
                    audit.record(id, user, "DEVICE_FAILED", device.getName() + ": " + e.getMessage());
                    failed = true;
                    continue;
                }

                if (!client.isHealthy(device)) {
                    restore(device, before);
                    ex.mark(ExecutionStatus.ROLLED_BACK, "Health check failed after change; previous config restored");
                    executions.save(ex);
                    audit.record(id, user, "DEVICE_UNHEALTHY", device.getName() + " failed health check, restored");
                    failed = true;
                    continue;
                }

                ex.mark(ExecutionStatus.APPLIED, before.equals(after) ? "No change needed" : "Applied");
                executions.save(ex);
                applied.add(new Applied(device, ex));
                audit.record(id, user, "DEVICE_APPLIED", device.getName() + " (" + stageName(s) + ")");
            }
        }

        if (failed) {
            for (int i = applied.size() - 1; i >= 0; i--) {
                Applied a = applied.get(i);
                restore(a.device(), a.execution().getPreviousConfig());
                a.execution().mark(ExecutionStatus.ROLLED_BACK, "Automatically rolled back after a later device failed");
                executions.save(a.execution());
                audit.record(id, "system", "AUTO_ROLLBACK", a.device().getName() + " restored");
            }
            change.setStatus(ChangeStatus.FAILED);
            audit.record(id, user, "DEPLOY_FAILED", "Rollout stopped; changed devices were restored");
        } else {
            change.setStatus(ChangeStatus.DEPLOYED);
            audit.record(id, user, "DEPLOYED", "All devices updated");
        }
        return change;
    }

    @Transactional
    public ChangeRequest rollback(Long id, String user) {
        ChangeRequest change = get(id);
        requireStatus(change, ChangeStatus.DEPLOYED, "rolled back");
        List<DeviceExecution> done = executions.findByChangeIdOrderByStepOrderAsc(id);
        for (int i = done.size() - 1; i >= 0; i--) {
            DeviceExecution ex = done.get(i);
            if (ex.getStatus() != ExecutionStatus.APPLIED) {
                continue;
            }
            Device device = devices.findById(ex.getDeviceId())
                    .orElseThrow(() -> NetChangeException.notFound("Device " + ex.getDeviceName() + " no longer exists"));
            restore(device, ex.getPreviousConfig());
            ex.mark(ExecutionStatus.ROLLED_BACK, "Rolled back by " + user);
            executions.save(ex);
            audit.record(id, user, "DEVICE_ROLLED_BACK", device.getName() + " restored");
        }
        change.setStatus(ChangeStatus.ROLLED_BACK);
        audit.record(id, user, "ROLLED_BACK", "Change reverted on all devices");
        return change;
    }

    // ---------------------------------------------------------------- helpers

    /** Restore failures are logged rather than thrown so the remaining devices are still restored. */
    private void restore(Device device, String config) {
        try {
            client.applyConfig(device, config);
        } catch (DeviceException e) {
            audit.record(null, "system", "RESTORE_FAILED", device.getName() + ": " + e.getMessage());
        }
    }

    private List<Device> targetDevices(ChangeRequest change) {
        List<Device> targets = devices.findByDeviceGroupOrderByNameAsc(change.getTargetGroup());
        if (targets.isEmpty()) {
            throw NetChangeException.conflict("No devices in group '" + change.getTargetGroup() + "'");
        }
        return targets;
    }

    private List<String> precheck(ChangeRequest change, List<Device> targets) {
        List<String> problems = new ArrayList<>();
        for (Device d : targets) {
            if (!isCompatible(change, d)) {
                problems.add(d.getName() + " runs " + d.getOsVersion()
                        + " but the change needs " + change.getMinOsVersion() + " or newer");
            }
        }
        return problems;
    }

    private boolean isCompatible(ChangeRequest change, Device device) {
        String min = change.getMinOsVersion();
        return min == null || min.isBlank() || VersionUtil.compare(device.getOsVersion(), min) >= 0;
    }

    /** First stage is one canary device; after that devices go in batches. */
    static List<List<Device>> stages(List<Device> targets, int batchSize) {
        int size = Math.max(1, batchSize);
        List<List<Device>> stages = new ArrayList<>();
        stages.add(List.of(targets.get(0)));
        for (int i = 1; i < targets.size(); i += size) {
            stages.add(targets.subList(i, Math.min(i + size, targets.size())));
        }
        return stages;
    }

    private static String stageName(int index) {
        return index == 0 ? "CANARY" : "BATCH-" + index;
    }

    private static void requireStatus(ChangeRequest change, ChangeStatus expected, String verb) {
        if (change.getStatus() != expected) {
            throw NetChangeException.conflict("Change " + change.getId() + " is " + change.getStatus()
                    + " and cannot be " + verb + " (needs " + expected + ")");
        }
    }
}
