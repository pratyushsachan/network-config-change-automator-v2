package com.netchange.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.netchange.domain.AuditEvent;
import com.netchange.domain.ChangeRequest;
import com.netchange.domain.ChangeStatus;
import com.netchange.domain.Device;
import com.netchange.domain.DeviceExecution;
import com.netchange.domain.ExecutionStatus;
import com.netchange.domain.FaultMode;
import com.netchange.errors.NetChangeException;
import com.netchange.repo.AuditEventRepository;
import com.netchange.repo.ChangeRequestRepository;
import com.netchange.repo.DeviceExecutionRepository;
import com.netchange.repo.DeviceRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChangeWorkflowTest {

    private static final String NEW_NTP = "ntp server 10.10.0.200";

    @Autowired ChangeService service;
    @Autowired DeviceRepository devices;
    @Autowired ChangeRequestRepository changes;
    @Autowired DeviceExecutionRepository executions;
    @Autowired AuditEventRepository auditEvents;

    @BeforeEach
    void cleanDatabase() {
        auditEvents.deleteAll();
        executions.deleteAll();
        changes.deleteAll();
        devices.deleteAll();
    }

    private Device device(String name, String group, String os, FaultMode fault) {
        return devices.save(new Device(name, "10.0.0.1", "Catalyst 9300", os, "Lab", group,
                "hostname " + name + "\nsnmp-server community public RO", fault));
    }

    private ChangeRequest approvedChange(String group, String lines, String minOs, int batchSize) {
        ChangeRequest c = service.create("Update NTP", "test", group, lines, minOs, batchSize, "alice");
        return service.approve(c.getId(), "bob");
    }

    private String config(String deviceName) {
        return devices.findAll().stream().filter(d -> d.getName().equals(deviceName))
                .findFirst().orElseThrow().getRunningConfig();
    }

    @Test
    void requesterCannotApproveOwnChange() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        ChangeRequest c = service.create("t", "r", "g", NEW_NTP, null, 2, "alice");

        assertThatThrownBy(() -> service.approve(c.getId(), "Alice"))
                .isInstanceOf(NetChangeException.class).hasMessageContaining("own change");

        assertThat(service.get(c.getId()).getStatus()).isEqualTo(ChangeStatus.PENDING_APPROVAL);
        assertThat(service.dryRun(c.getId()).devices()).hasSize(1);
        // The denied attempt is still recorded in the audit log.
        assertThat(auditEvents.findByChangeIdOrderByIdAsc(c.getId()))
                .extracting(AuditEvent::getAction).contains("APPROVAL_DENIED");
    }

    @Test
    void cannotDeployBeforeApproval() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        ChangeRequest c = service.create("t", "r", "g", NEW_NTP, null, 2, "alice");

        assertThatThrownBy(() -> service.deploy(c.getId(), "alice"))
                .isInstanceOf(NetChangeException.class).hasMessageContaining("PENDING_APPROVAL");
    }

    @Test
    void createFailsForUnknownGroup() {
        assertThatThrownBy(() -> service.create("t", "r", "nope", NEW_NTP, null, 2, "alice"))
                .isInstanceOf(NetChangeException.class).hasMessageContaining("No devices");
    }

    @Test
    void dryRunShowsDiffWithoutTouchingDevices() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        device("sw-2", "g", "17.9", FaultMode.NONE);
        device("sw-3", "g", "17.9", FaultMode.NONE);
        ChangeRequest c = approvedChange("g", NEW_NTP + "\nno snmp-server community public RO", null, 2);

        DryRunResult result = service.dryRun(c.getId());

        assertThat(result.deployable()).isTrue();
        assertThat(result.devices()).extracting(DryRunResult.DeviceDiff::stage)
                .containsExactly("CANARY", "BATCH-1", "BATCH-1");
        assertThat(result.devices().get(0).added()).containsExactly(NEW_NTP);
        assertThat(result.devices().get(0).removed()).containsExactly("snmp-server community public RO");
        assertThat(config("sw-1")).contains("snmp-server community public RO").doesNotContain(NEW_NTP);
    }

    @Test
    void successfulStagedDeployUpdatesEveryDevice() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        device("sw-2", "g", "17.9", FaultMode.NONE);
        device("sw-3", "g", "17.9", FaultMode.NONE);
        ChangeRequest c = approvedChange("g", NEW_NTP, null, 2);

        ChangeRequest result = service.deploy(c.getId(), "alice");

        assertThat(result.getStatus()).isEqualTo(ChangeStatus.DEPLOYED);
        List<DeviceExecution> steps = service.executionsFor(c.getId());
        assertThat(steps).extracting(DeviceExecution::getStage).containsExactly("CANARY", "BATCH-1", "BATCH-1");
        assertThat(steps).allMatch(s -> s.getStatus() == ExecutionStatus.APPLIED);
        assertThat(config("sw-1")).contains(NEW_NTP);
        assertThat(config("sw-3")).contains(NEW_NTP);
    }

    @Test
    void failureStopsRolloutSkipsRemainingAndRestoresChangedDevices() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        device("sw-2", "g", "17.9", FaultMode.NONE);
        device("sw-3", "g", "17.9", FaultMode.APPLY_FAILS);
        device("sw-4", "g", "17.9", FaultMode.NONE);
        String before1 = config("sw-1");
        ChangeRequest c = approvedChange("g", NEW_NTP, null, 2);

        ChangeRequest result = service.deploy(c.getId(), "alice");

        assertThat(result.getStatus()).isEqualTo(ChangeStatus.FAILED);
        assertThat(service.executionsFor(c.getId())).extracting(DeviceExecution::getStatus)
                .containsExactly(ExecutionStatus.ROLLED_BACK, ExecutionStatus.ROLLED_BACK,
                        ExecutionStatus.FAILED, ExecutionStatus.SKIPPED);
        assertThat(config("sw-1")).isEqualTo(before1).doesNotContain(NEW_NTP);
        assertThat(config("sw-2")).doesNotContain(NEW_NTP);
        assertThat(config("sw-4")).doesNotContain(NEW_NTP);
        assertThat(auditEvents.findByChangeIdOrderByIdAsc(c.getId())).extracting(AuditEvent::getAction)
                .contains("DEVICE_FAILED", "AUTO_ROLLBACK", "DEPLOY_FAILED");
    }

    @Test
    void canaryFailureStopsBeforeAnyOtherDeviceIsTouched() {
        device("sw-1", "g", "17.9", FaultMode.APPLY_FAILS);
        device("sw-2", "g", "17.9", FaultMode.NONE);
        ChangeRequest c = approvedChange("g", NEW_NTP, null, 2);

        service.deploy(c.getId(), "alice");

        assertThat(service.executionsFor(c.getId())).extracting(DeviceExecution::getStatus)
                .containsExactly(ExecutionStatus.FAILED, ExecutionStatus.SKIPPED);
        assertThat(config("sw-2")).doesNotContain(NEW_NTP);
    }

    @Test
    void unhealthyDeviceIsRestoredAndStopsTheRollout() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        device("sw-2", "g", "17.9", FaultMode.UNHEALTHY_AFTER_CHANGE);
        ChangeRequest c = approvedChange("g", NEW_NTP, null, 1);

        ChangeRequest result = service.deploy(c.getId(), "alice");

        assertThat(result.getStatus()).isEqualTo(ChangeStatus.FAILED);
        assertThat(config("sw-1")).doesNotContain(NEW_NTP);
        assertThat(config("sw-2")).doesNotContain(NEW_NTP);
        assertThat(auditEvents.findByChangeIdOrderByIdAsc(c.getId())).extracting(AuditEvent::getAction)
                .contains("DEVICE_UNHEALTHY");
    }

    @Test
    void osVersionPreCheckBlocksDeploymentAndIsAudited() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        device("sw-2", "g", "16.12.5", FaultMode.NONE);
        ChangeRequest c = approvedChange("g", NEW_NTP, "17.3", 2);

        assertThat(service.dryRun(c.getId()).deployable()).isFalse();
        assertThatThrownBy(() -> service.deploy(c.getId(), "alice"))
                .isInstanceOf(NetChangeException.class).hasMessageContaining("sw-2 runs 16.12.5");

        assertThat(config("sw-1")).doesNotContain(NEW_NTP);
        assertThat(service.get(c.getId()).getStatus()).isEqualTo(ChangeStatus.APPROVED);
        assertThat(auditEvents.findByChangeIdOrderByIdAsc(c.getId())).extracting(AuditEvent::getAction)
                .contains("DEPLOY_BLOCKED");
    }

    @Test
    void manualRollbackRestoresOriginalConfigs() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        device("sw-2", "g", "17.9", FaultMode.NONE);
        String original = config("sw-1");
        ChangeRequest c = approvedChange("g", NEW_NTP + "\nno snmp-server community public RO", null, 2);
        service.deploy(c.getId(), "alice");
        assertThat(config("sw-1")).contains(NEW_NTP).doesNotContain("snmp-server community public RO");

        ChangeRequest result = service.rollback(c.getId(), "carol");

        assertThat(result.getStatus()).isEqualTo(ChangeStatus.ROLLED_BACK);
        assertThat(config("sw-1")).isEqualTo(original);
        assertThat(service.executionsFor(c.getId())).allMatch(s -> s.getStatus() == ExecutionStatus.ROLLED_BACK);
    }

    @Test
    void auditTrailRecordsEveryStepInOrderWithActors() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        ChangeRequest c = approvedChange("g", NEW_NTP, null, 2);
        service.deploy(c.getId(), "alice");
        service.rollback(c.getId(), "carol");

        List<AuditEvent> trail = auditEvents.findByChangeIdOrderByIdAsc(c.getId());

        assertThat(trail).extracting(AuditEvent::getAction).containsExactly(
                "CREATED", "APPROVED", "DEPLOY_STARTED", "DEVICE_APPLIED", "DEPLOYED",
                "DEVICE_ROLLED_BACK", "ROLLED_BACK");
        assertThat(trail.get(0).getActor()).isEqualTo("alice");
        assertThat(trail.get(1).getActor()).isEqualTo("bob");
        assertThat(trail.get(6).getActor()).isEqualTo("carol");
    }

    @Test
    void rejectedChangeCannotBeDeployed() {
        device("sw-1", "g", "17.9", FaultMode.NONE);
        ChangeRequest c = service.create("t", "r", "g", NEW_NTP, null, 2, "alice");
        service.reject(c.getId(), "bob", "Wrong NTP server");

        assertThatThrownBy(() -> service.deploy(c.getId(), "alice"))
                .isInstanceOf(NetChangeException.class).hasMessageContaining("REJECTED");
    }
}
