package com.example.graphql.exception;

import lombok.Getter;

@Getter
public abstract class GraphQLException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String userMessage;

    protected GraphQLException(ErrorCode errorCode, String userMessage) {
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    protected GraphQLException(ErrorCode errorCode, String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
}
