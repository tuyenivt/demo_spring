package com.coloza.sample.rabbitmq.exception;

public class PaymentProcessingException extends Exception {
    public PaymentProcessingException(String message) {
        super(message);
    }
}
