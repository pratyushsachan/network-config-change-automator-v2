package com.netchange.service;

import com.netchange.domain.AuditEvent;
import com.netchange.repo.AuditEventRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditEventRepository events;

    public AuditService(AuditEventRepository events) {
        this.events = events;
    }

    public void record(Long changeId, String actor, String action, String detail) {
        events.save(new AuditEvent(changeId, actor, action, detail));
    }

    public List<AuditEvent> forChange(Long changeId) {
        return events.findByChangeIdOrderByIdAsc(changeId);
    }

    public List<AuditEvent> all() {
        return events.findAllByOrderByIdDesc();
    }
}
