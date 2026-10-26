package com.netchange.repo;

import com.netchange.domain.Device;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByDeviceGroupOrderByNameAsc(String deviceGroup);
    boolean existsByName(String name);
}
