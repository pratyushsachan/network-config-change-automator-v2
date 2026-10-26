package com.netchange.config;

import com.netchange.domain.Device;
import com.netchange.domain.FaultMode;
import com.netchange.repo.DeviceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Loads a small simulated network so the API can be tried immediately. */
@Component
@ConditionalOnProperty(name = "netchange.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final DeviceRepository devices;

    public DataSeeder(DeviceRepository devices) {
        this.devices = devices;
    }

    @Override
    public void run(String... args) {
        if (devices.count() > 0) {
            return;
        }
        // campus-switches: one switch runs an old OS, which a "17.3 or newer" change will block.
        add("sw-del-01", "10.10.1.11", "Catalyst 9300", "17.9.4", "Delhi", "campus-switches", FaultMode.NONE);
        add("sw-del-02", "10.10.1.12", "Catalyst 9300", "17.9.4", "Delhi", "campus-switches", FaultMode.NONE);
        add("sw-noi-01", "10.10.2.11", "Catalyst 9200", "17.6.5", "Noida", "campus-switches", FaultMode.NONE);
        add("sw-noi-02", "10.10.2.12", "Catalyst 9200", "16.12.5", "Noida", "campus-switches", FaultMode.NONE);

        // core-routers: all healthy, a clean successful rollout.
        add("rtr-core-01", "10.10.0.1", "ISR 4451", "17.9.4", "Delhi", "core-routers", FaultMode.NONE);
        add("rtr-core-02", "10.10.0.2", "ISR 4451", "17.9.4", "Noida", "core-routers", FaultMode.NONE);

        // lab-switches: the third device rejects changes, which triggers automatic rollback.
        add("sw-lab-01", "10.20.0.11", "Catalyst 9300", "17.9.4", "Lab", "lab-switches", FaultMode.NONE);
        add("sw-lab-02", "10.20.0.12", "Catalyst 9300", "17.9.4", "Lab", "lab-switches", FaultMode.NONE);
        add("sw-lab-03", "10.20.0.13", "Catalyst 9300", "17.9.4", "Lab", "lab-switches", FaultMode.APPLY_FAILS);
        add("sw-lab-04", "10.20.0.14", "Catalyst 9300", "17.9.4", "Lab", "lab-switches", FaultMode.NONE);
    }

    private void add(String name, String ip, String model, String os, String site, String group, FaultMode fault) {
        String config = String.join("\n",
                "hostname " + name,
                "ntp server 10.10.0.100",
                "snmp-server community public RO",
                "ip ssh version 2");
        devices.save(new Device(name, ip, model, os, site, group, config, fault));
    }
}
