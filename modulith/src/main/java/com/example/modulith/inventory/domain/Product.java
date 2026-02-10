package com.example.modulith.inventory.domain;

import com.example.modulith.inventory.InsufficientStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    public Product(String sku, String name, BigDecimal price, Integer stockQuantity) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public boolean hasStock(int quantity) {
        return this.stockQuantity >= quantity;
    }

    public void reserveStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new InsufficientStockException(sku, quantity, stockQuantity);
        }
        this.stockQuantity -= quantity;
    }

    public void releaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void updateDetails(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    public void restock(int quantity) {
        this.stockQuantity += quantity;
    }
}
