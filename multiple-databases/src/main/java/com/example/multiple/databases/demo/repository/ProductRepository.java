package com.example.multiple.databases.demo.repository;

import com.example.multiple.databases.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
