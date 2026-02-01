package com.example.database.migration.demo.repository;

import com.example.database.migration.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
