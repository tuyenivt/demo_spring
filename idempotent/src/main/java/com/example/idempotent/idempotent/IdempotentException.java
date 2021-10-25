package com.example.idempotent.idempotent;

public class IdempotentException extends RuntimeException {

    public IdempotentException() {
    }

    public IdempotentException(String msg) {
        super(msg);
    }

    public IdempotentException(Throwable cause) {
        super(cause);
    }

    public IdempotentException(String message, Throwable cause) {
        super(message, cause);
    }
}
