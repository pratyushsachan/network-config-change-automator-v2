package com.netchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class ChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String reason;

    @Column(nullable = false)
    private String targetGroup;

    /** One config line per row. A line starting with "no " removes the matching line. */
    @Column(nullable = false, length = 10000)
    private String configLines;

    /** Devices running an older OS than this are blocked by the pre-check. May be empty. */
    private String minOsVersion;

    /** Number of devices per batch after the single-device canary stage. */
    private int batchSize = 2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeStatus status = ChangeStatus.PENDING_APPROVAL;

    @Column(nullable = false)
    private String requestedBy;

    private String approvedBy;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    protected ChangeRequest() {
    }

    public ChangeRequest(String title, String reason, String targetGroup, String configLines,
                         String minOsVersion, int batchSize, String requestedBy) {
        this.title = title;
        this.reason = reason;
        this.targetGroup = targetGroup;
        this.configLines = configLines;
        this.minOsVersion = minOsVersion;
        this.batchSize = batchSize;
        this.requestedBy = requestedBy;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getReason() { return reason; }
    public String getTargetGroup() { return targetGroup; }
    public String getConfigLines() { return configLines; }
    public String getMinOsVersion() { return minOsVersion; }
    public int getBatchSize() { return batchSize; }
    public ChangeStatus getStatus() { return status; }
    public String getRequestedBy() { return requestedBy; }
    public String getApprovedBy() { return approvedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(ChangeStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
}
