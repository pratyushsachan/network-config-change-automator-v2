package com.netchange.domain;

public enum ChangeStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    DEPLOYED,
    /** Deployment stopped because a device failed; devices already changed were restored. */
    FAILED,
    /** A deployed change was reverted by an operator. */
    ROLLED_BACK
}
