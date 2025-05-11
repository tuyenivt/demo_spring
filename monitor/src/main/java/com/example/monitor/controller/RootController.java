package com.example.monitor.controller;

import com.example.monitor.entity.Customer;
import com.example.monitor.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

@RestController
@RequiredArgsConstructor
public class RootController {
    private final CustomerRepository repository;
    private final Random random;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/customers")
    public List<Customer> all() {
        return repository.findAll();
    }

    @GetMapping("/customers/transform")
    public List<Customer> getCustomersTransform() throws InterruptedException {
        var customers = repository.findAll();
        Thread.sleep(random.nextLong(5000)); //slow operation
        return customers;
    }
}
