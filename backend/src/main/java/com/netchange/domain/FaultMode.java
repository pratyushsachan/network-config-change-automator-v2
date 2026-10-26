package com.netchange.domain;

/** Failure behaviour of a simulated device, used to demonstrate and test rollback logic. */
public enum FaultMode {
    NONE,
    /** The device rejects the change (for example an SSH session timeout). */
    APPLY_FAILS,
    /** The change is accepted but the device fails its health check afterwards. */
    UNHEALTHY_AFTER_CHANGE
}
