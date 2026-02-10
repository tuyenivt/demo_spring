package com.example.versioning.exception;

import org.springframework.http.HttpStatus;

public class ApiVersionException extends RuntimeException {

    private final String requestedVersion;
    private final HttpStatus status;

    public ApiVersionException(String message, String requestedVersion, HttpStatus status) {
        super(message);
        this.requestedVersion = requestedVersion;
        this.status = status;
    }

    public String getRequestedVersion() {
        return requestedVersion;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
