package com.netchange.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateChangeRequest(
        @NotBlank String title,
        String reason,
        @NotBlank String targetGroup,
        @NotBlank String configLines,
        String minOsVersion,
        @Min(1) Integer batchSize) {
}
