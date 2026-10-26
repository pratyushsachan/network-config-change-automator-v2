package com.netchange.web;

import com.netchange.domain.ChangeRequest;
import com.netchange.domain.DeviceExecution;
import java.util.List;

public record ChangeDetails(ChangeRequest change, List<DeviceExecution> executions) {
}
