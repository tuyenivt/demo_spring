package com.example.modulith.inventory.application;

import com.example.modulith.inventory.*;
import com.example.modulith.inventory.domain.Product;
import com.example.modulith.inventory.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void reserveStock(ReserveStockCommand command) {
        var product = getProduct(command.sku());

        product.reserveStock(command.quantity());
        productRepository.save(product);

        eventPublisher.publishEvent(new StockReservedEvent(
                command.sku(),
                command.quantity(),
                product.getId()
        ));
    }

    @Transactional
    public void releaseStock(String sku, Integer quantity) {
        var product = getProduct(sku);
        product.releaseStock(quantity);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public boolean isInStock(String sku, int quantity) {
        return productRepository.findBySku(sku)
                .map(product -> product.hasStock(quantity))
                .orElse(false);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductCommand command) {
        if (productRepository.existsBySku(command.sku())) {
            throw new DuplicateSkuException(command.sku());
        }

        var product = new Product(command.sku(), command.name(), command.price(), command.stockQuantity());
        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(String sku, UpdateProductCommand command) {
        var product = getProduct(sku);
        product.updateDetails(command.name(), command.price());
        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse restockProduct(String sku, RestockProductCommand command) {
        var product = getProduct(sku);
        product.restock(command.quantity());
        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        return mapToResponse(getProduct(sku));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    private Product getProduct(String sku) {
        return productRepository.findBySku(sku).orElseThrow(() -> new ProductNotFoundException(sku));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
