package com.netchange.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConfigDiffTest {

    private static final String RUNNING = "hostname sw1\nntp server 10.0.0.1\nsnmp-server community public RO";

    @Test
    void addsNewLinesAndSkipsExistingOnes() {
        ConfigDiff.Result r = ConfigDiff.diff(RUNNING, "ntp server 10.0.0.1\nntp server 10.0.0.2");
        assertThat(r.added()).containsExactly("ntp server 10.0.0.2");
        assertThat(r.removed()).isEmpty();
    }

    @Test
    void noPrefixRemovesMatchingLine() {
        ConfigDiff.Result r = ConfigDiff.diff(RUNNING, "no snmp-server community public RO");
        assertThat(r.removed()).containsExactly("snmp-server community public RO");
        assertThat(r.added()).isEmpty();
    }

    @Test
    void removingAMissingLineIsANoOp() {
        assertThat(ConfigDiff.diff(RUNNING, "no ip http server").isEmpty()).isTrue();
    }

    @Test
    void ignoresBlankLinesAndComments() {
        assertThat(ConfigDiff.diff(RUNNING, "\n! a comment\n   \n").isEmpty()).isTrue();
    }

    @Test
    void applyProducesMergedConfigAndIsIdempotent() {
        String change = "no snmp-server community public RO\nntp server 10.0.0.2";
        String once = ConfigDiff.apply(RUNNING, change);
        assertThat(once).isEqualTo("hostname sw1\nntp server 10.0.0.1\nntp server 10.0.0.2");
        assertThat(ConfigDiff.apply(once, change)).isEqualTo(once);
    }
}
