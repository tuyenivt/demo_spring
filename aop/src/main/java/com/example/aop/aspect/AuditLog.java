package com.example.aop.aspect;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Audit log entry containing all relevant audit information.
 */
@Getter
@Builder
@ToString
public class AuditLog {

    private final Instant timestamp;
    private final String user;
    private final String action;
    private final String entity;
    private final String method;
    private final String args;
    private final String result;
}
