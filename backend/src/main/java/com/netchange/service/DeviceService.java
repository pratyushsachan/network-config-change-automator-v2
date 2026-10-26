package com.netchange.service;

import com.netchange.domain.Device;
import com.netchange.errors.NetChangeException;
import com.netchange.repo.DeviceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final DeviceRepository devices;

    public DeviceService(DeviceRepository devices) {
        this.devices = devices;
    }

    @Transactional
    public Device register(Device device) {
        if (devices.existsByName(device.getName())) {
            throw NetChangeException.conflict("Device " + device.getName() + " already exists");
        }
        return devices.save(device);
    }

    @Transactional(readOnly = true)
    public List<Device> list(String group) {
        return group == null || group.isBlank() ? devices.findAll() : devices.findByDeviceGroupOrderByNameAsc(group);
    }

    @Transactional(readOnly = true)
    public Device get(Long id) {
        return devices.findById(id).orElseThrow(() -> NetChangeException.notFound("Device " + id + " not found"));
    }
}
