package com.example.monitor.repository;

import com.example.monitor.entity.Customer;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    @Timed(value = "db.query", extraTags = {"entity", "customer", "operation", "findAll"})
    @Override
    List<Customer> findAll();
}