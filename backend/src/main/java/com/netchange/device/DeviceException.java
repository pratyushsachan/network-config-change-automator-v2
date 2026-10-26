package com.netchange.device;

/** A device could not be reached or rejected a configuration. */
public class DeviceException extends Exception {
    public DeviceException(String message) {
        super(message);
    }
}
