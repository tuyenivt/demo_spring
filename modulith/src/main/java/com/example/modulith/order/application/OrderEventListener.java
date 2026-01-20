package com.example.modulith.order.application;

import com.example.modulith.customer.CustomerRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listens to events from other modules.
 * Demonstrates asynchronous, event-driven communication between modules.
 */
@Slf4j
@Component
public class OrderEventListener {

    @ApplicationModuleListener
    public void on(CustomerRegisteredEvent event) {
        log.info("Order module received CustomerRegisteredEvent for customer: {} ({})", event.name(), event.customerId());

        // Could send welcome discount, initialize customer preferences, etc.
        // This is decoupled - customer module doesn't know about order module
    }
}
