package com.netchange.service;

import java.util.List;

/** What a deployment would do, without touching any device. */
public record DryRunResult(Long changeId, boolean deployable, List<String> problems, List<DeviceDiff> devices) {

    public record DeviceDiff(String device, String osVersion, String stage, boolean compatible,
                             List<String> added, List<String> removed) {
    }
}
