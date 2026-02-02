package com.example.rabbitmq.exception;

public class PaymentValidationException extends Exception {
    public PaymentValidationException(String message) {
        super(message);
    }
}
