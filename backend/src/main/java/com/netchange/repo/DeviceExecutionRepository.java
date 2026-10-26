package com.netchange.repo;

import com.netchange.domain.DeviceExecution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceExecutionRepository extends JpaRepository<DeviceExecution, Long> {
    List<DeviceExecution> findByChangeIdOrderByStepOrderAsc(Long changeId);
}
