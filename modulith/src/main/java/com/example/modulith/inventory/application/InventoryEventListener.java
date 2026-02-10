package com.example.modulith.inventory.application;

import com.example.modulith.inventory.ReserveStockCommand;
import com.example.modulith.order.OrderCancelledEvent;
import com.example.modulith.order.OrderCreatedEvent;
import com.example.modulith.order.StockReservationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    @ApplicationModuleListener
    public void on(OrderCreatedEvent event) {
        log.info("Inventory module received OrderCreatedEvent for order: {}", event.orderId());

        try {
            inventoryService.reserveStock(new ReserveStockCommand(event.sku(), event.quantity()));
            log.info("Reserved stock for order {} (sku={}, quantity={})", event.orderId(), event.sku(), event.quantity());
        } catch (RuntimeException ex) {
            log.warn("Stock reservation failed for order {} (sku={}, quantity={}): {}",
                    event.orderId(), event.sku(), event.quantity(), ex.getMessage());
            eventPublisher.publishEvent(new StockReservationFailedEvent(
                    event.orderId(),
                    event.sku(),
                    event.quantity(),
                    ex.getMessage(),
                    Instant.now()
            ));
        }
    }

    @ApplicationModuleListener
    public void on(OrderCancelledEvent event) {
        inventoryService.releaseStock(event.sku(), event.quantity());
        log.info("Released stock for cancelled order {} (sku={}, quantity={})", event.orderId(), event.sku(), event.quantity());
    }
}
