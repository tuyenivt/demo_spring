package com.coloza.sample.rabbitmq.exception;

public class PaymentValidationException extends Exception {
    public PaymentValidationException(String message) {
        super(message);
    }
}
