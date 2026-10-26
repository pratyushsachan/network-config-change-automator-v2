package com.netchange.device;

import com.netchange.domain.Device;

/**
 * How the service talks to a network device. The bundled implementation is simulated;
 * a real one would use SSH (CLI) or NETCONF against Cisco IOS XE, for example.
 */
public interface DeviceClient {

    String getRunningConfig(Device device);

    /** Replaces the device's running configuration with {@code newConfig}. */
    void applyConfig(Device device, String newConfig) throws DeviceException;

    /** Post-change health check (reachability, interfaces up, and so on). */
    boolean isHealthy(Device device);
}
