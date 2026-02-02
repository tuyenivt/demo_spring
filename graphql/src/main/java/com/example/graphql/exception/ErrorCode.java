package com.example.graphql.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Validation errors (4xx)
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed", 400),
    INVALID_INPUT("INVALID_INPUT", "Invalid input provided", 400),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource not found", 404),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Resource already exists", 409),

    // Business errors (4xx)
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "Business rule violated", 422),
    STUDENT_AGE_INVALID("STUDENT_AGE_INVALID", "Student age is invalid", 422),
    VEHICLE_ASSIGNMENT_ERROR("VEHICLE_ASSIGNMENT_ERROR", "Cannot assign vehicle to student", 422),
    STUDENT_HAS_VEHICLES("STUDENT_HAS_VEHICLES", "Cannot delete student with vehicles", 409),

    // Technical errors (5xx) - These will be masked in production
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "An unexpected error occurred", 500),
    DATABASE_ERROR("DATABASE_ERROR", "Database operation failed", 500),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "External service error", 503);

    private final String code;
    private final String message;
    private final int statusCode;

    ErrorCode(String code, String message, int statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public boolean isTechnicalError() {
        return statusCode >= 500;
    }
}
