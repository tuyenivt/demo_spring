package com.example.graphql.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ValidationException extends GraphQLException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.fieldErrors = new HashMap<>();
    }
}
