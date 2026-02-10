package com.example.modulith.inventory;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String sku) {
        super("Product not found: " + sku);
    }
}
