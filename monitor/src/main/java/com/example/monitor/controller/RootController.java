package com.example.monitor.controller;

import com.example.monitor.entity.Customer;
import com.example.monitor.repository.CustomerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
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
    private final Counter customerAccessCounter;
    private final Timer customerTransformTimer;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/customers")
    public List<Customer> all() {
        customerAccessCounter.increment();
        return repository.findAll();
    }

    @GetMapping("/customers/transform")
    public List<Customer> getCustomersTransform() {
        return customerTransformTimer.record(() -> {
            var customers = repository.findAll();
            try {
                Thread.sleep(random.nextLong(5000)); //slow operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return customers;
        });
    }
}
