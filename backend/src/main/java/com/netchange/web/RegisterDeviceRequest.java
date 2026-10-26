package com.netchange.web;

import com.netchange.domain.FaultMode;
import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceRequest(
        @NotBlank String name,
        String ipAddress,
        String model,
        String osVersion,
        String site,
        @NotBlank String deviceGroup,
        String runningConfig,
        FaultMode faultMode) {
}
