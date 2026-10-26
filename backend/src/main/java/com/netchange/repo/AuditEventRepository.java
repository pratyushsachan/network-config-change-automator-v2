package com.netchange.repo;

import com.netchange.domain.AuditEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByChangeIdOrderByIdAsc(Long changeId);
    List<AuditEvent> findAllByOrderByIdDesc();
}
