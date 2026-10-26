package com.netchange.errors;

import org.springframework.http.HttpStatus;

/** Business-rule failure that maps directly to an HTTP status. */
public class NetChangeException extends RuntimeException {

    private final HttpStatus status;

    private NetChangeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static NetChangeException notFound(String message) {
        return new NetChangeException(HttpStatus.NOT_FOUND, message);
    }

    public static NetChangeException conflict(String message) {
        return new NetChangeException(HttpStatus.CONFLICT, message);
    }

    public static NetChangeException forbidden(String message) {
        return new NetChangeException(HttpStatus.FORBIDDEN, message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
