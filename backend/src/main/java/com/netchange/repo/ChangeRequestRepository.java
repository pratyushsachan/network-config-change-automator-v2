package com.netchange.repo;

import com.netchange.domain.ChangeRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {
    List<ChangeRequest> findAllByOrderByIdDesc();
}
