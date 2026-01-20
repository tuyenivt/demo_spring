package com.example.modulith.inventory.application;

import com.example.modulith.inventory.ReserveStockCommand;
import com.example.modulith.inventory.StockReservedEvent;
import com.example.modulith.inventory.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void reserveStock(ReserveStockCommand command) {
        var product = productRepository.findBySku(command.sku())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + command.sku()));

        product.reserveStock(command.quantity());
        productRepository.save(product);

        eventPublisher.publishEvent(new StockReservedEvent(
                command.sku(),
                command.quantity(),
                product.getId()
        ));
    }

    public boolean isInStock(String sku, int quantity) {
        return productRepository.findBySku(sku)
                .map(product -> product.hasStock(quantity))
                .orElse(false);
    }
}
