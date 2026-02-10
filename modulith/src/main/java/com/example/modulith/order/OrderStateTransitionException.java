package com.example.modulith.order;

public class OrderStateTransitionException extends RuntimeException {

    public OrderStateTransitionException(Long orderId, String currentState, String attemptedTransition) {
        super("Invalid order transition for order " + orderId + ": " + attemptedTransition + " from state " + currentState);
    }
}
