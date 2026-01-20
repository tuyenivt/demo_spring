package com.example.modulith.inventory.application;

import com.example.modulith.order.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryEventListener {

    @ApplicationModuleListener
    public void on(OrderCreatedEvent event) {
        log.info("Inventory module received OrderCreatedEvent for order: {}", event.orderId());

        // In a real system, this would trigger stock reservation for order items
        // For now, just logging to demonstrate inter-module communication
    }
}
