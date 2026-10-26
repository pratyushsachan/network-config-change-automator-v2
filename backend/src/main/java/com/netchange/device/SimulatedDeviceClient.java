package com.netchange.device;

import com.netchange.domain.Device;
import com.netchange.domain.FaultMode;
import com.netchange.repo.DeviceRepository;
import org.springframework.stereotype.Component;

/** Keeps each device's "running config" in the database and injects faults on request. */
@Component
public class SimulatedDeviceClient implements DeviceClient {

    private final DeviceRepository devices;

    public SimulatedDeviceClient(DeviceRepository devices) {
        this.devices = devices;
    }

    @Override
    public String getRunningConfig(Device device) {
        return device.getRunningConfig();
    }

    @Override
    public void applyConfig(Device device, String newConfig) throws DeviceException {
        if (device.getFaultMode() == FaultMode.APPLY_FAILS) {
            throw new DeviceException("SSH session to " + device.getName() + " timed out while applying config");
        }
        device.setRunningConfig(newConfig);
        devices.save(device);
    }

    @Override
    public boolean isHealthy(Device device) {
        return device.getFaultMode() != FaultMode.UNHEALTHY_AFTER_CHANGE;
    }
}
