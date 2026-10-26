package com.netchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

/** Append-only record of who did what and when. */
@Entity
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long changeId;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String action;

    @Column(length = 2000)
    private String detail;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected AuditEvent() {
    }

    public AuditEvent(Long changeId, String actor, String action, String detail) {
        this.changeId = changeId;
        this.actor = actor;
        this.action = action;
        this.detail = detail;
    }

    public Long getId() { return id; }
    public Long getChangeId() { return changeId; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }
}
