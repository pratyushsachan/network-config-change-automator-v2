package com.netchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String ipAddress;
    private String model;
    private String osVersion;
    private String site;

    @Column(name = "device_group", nullable = false)
    private String deviceGroup;

    @Column(length = 20000)
    private String runningConfig = "";

    @Enumerated(EnumType.STRING)
    private FaultMode faultMode = FaultMode.NONE;

    protected Device() {
    }

    public Device(String name, String ipAddress, String model, String osVersion,
                  String site, String deviceGroup, String runningConfig, FaultMode faultMode) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.model = model;
        this.osVersion = osVersion;
        this.site = site;
        this.deviceGroup = deviceGroup;
        this.runningConfig = runningConfig == null ? "" : runningConfig;
        this.faultMode = faultMode == null ? FaultMode.NONE : faultMode;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getIpAddress() { return ipAddress; }
    public String getModel() { return model; }
    public String getOsVersion() { return osVersion; }
    public String getSite() { return site; }
    public String getDeviceGroup() { return deviceGroup; }
    public String getRunningConfig() { return runningConfig; }
    public FaultMode getFaultMode() { return faultMode; }

    public void setRunningConfig(String runningConfig) { this.runningConfig = runningConfig; }
    public void setFaultMode(FaultMode faultMode) { this.faultMode = faultMode; }
}
