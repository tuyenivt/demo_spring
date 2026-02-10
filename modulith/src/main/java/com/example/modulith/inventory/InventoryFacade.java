package com.example.modulith.inventory;

import com.example.modulith.inventory.application.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public ProductResponse createProduct(CreateProductCommand command) {
        return inventoryService.createProduct(command);
    }

    public ProductResponse updateProduct(String sku, UpdateProductCommand command) {
        return inventoryService.updateProduct(sku, command);
    }

    public ProductResponse restockProduct(String sku, RestockProductCommand command) {
        return inventoryService.restockProduct(sku, command);
    }

    public ProductResponse getProductBySku(String sku) {
        return inventoryService.getProductBySku(sku);
    }

    public Page<ProductResponse> listProducts(Pageable pageable) {
        return inventoryService.listProducts(pageable);
    }
}
