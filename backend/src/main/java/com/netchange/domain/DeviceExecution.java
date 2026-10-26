package com.netchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

/** What happened to one device during one deployment. Holds the config needed for rollback. */
@Entity
public class DeviceExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long changeId;

    @Column(nullable = false)
    private Long deviceId;

    private String deviceName;
    private int stepOrder;

    /** CANARY, BATCH-1, BATCH-2, ... */
    private String stage;

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(length = 20000)
    private String previousConfig;

    @Column(length = 20000)
    private String newConfig;

    @Column(length = 1000)
    private String message;

    private Instant updatedAt = Instant.now();

    protected DeviceExecution() {
    }

    public DeviceExecution(Long changeId, Device device, int stepOrder, String stage) {
        this.changeId = changeId;
        this.deviceId = device.getId();
        this.deviceName = device.getName();
        this.stepOrder = stepOrder;
        this.stage = stage;
    }

    public Long getId() { return id; }
    public Long getChangeId() { return changeId; }
    public Long getDeviceId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public int getStepOrder() { return stepOrder; }
    public String getStage() { return stage; }
    public ExecutionStatus getStatus() { return status; }
    public String getPreviousConfig() { return previousConfig; }
    public String getNewConfig() { return newConfig; }
    public String getMessage() { return message; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setPreviousConfig(String previousConfig) { this.previousConfig = previousConfig; }
    public void setNewConfig(String newConfig) { this.newConfig = newConfig; }

    public void mark(ExecutionStatus status, String message) {
        this.status = status;
        this.message = message;
        this.updatedAt = Instant.now();
    }
}
