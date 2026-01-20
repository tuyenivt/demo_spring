package com.example.modulith.inventory;

import com.example.modulith.inventory.application.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryFacade {

    private final InventoryService inventoryService;

    public void reserveStock(ReserveStockCommand command) {
        inventoryService.reserveStock(command);
    }

    public boolean isInStock(String sku, int quantity) {
        return inventoryService.isInStock(sku, quantity);
    }
}
