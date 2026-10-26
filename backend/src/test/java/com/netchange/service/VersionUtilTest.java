package com.netchange.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionUtilTest {

    @Test
    void comparesNumericPartsNotStrings() {
        assertThat(VersionUtil.compare("17.9.4", "17.3")).isPositive();
        assertThat(VersionUtil.compare("16.12.5", "17.3")).isNegative();
        assertThat(VersionUtil.compare("17.10", "17.9")).isPositive();
    }

    @Test
    void missingPartsCountAsZero() {
        assertThat(VersionUtil.compare("17.3", "17.3.0")).isZero();
    }
}
