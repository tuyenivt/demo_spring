package com.example.monitor.controller;

import com.example.monitor.entity.Customer;
import com.example.monitor.health.ExternalApiHealthIndicator;
import com.example.monitor.repository.CustomerRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RootController {
    private final CustomerRepository repository;
    private final Random random;
    private final ExternalApiHealthIndicator externalApiHealthIndicator;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @Observed(name = "customer.list", contextualName = "list-customers")
    @Counted(value = "customer.access", description = "Number of customer list accesses")
    @GetMapping("/customers")
    public List<Customer> all() {
        log.info("Listing customers");
        return repository.findAll();
    }

    @Observed(name = "customer.transform.observed", contextualName = "transform-customers")
    @Timed(value = "customer.transform", description = "Customer transform operation timing")
    @GetMapping("/customers/transform")
    public List<Customer> getCustomersTransform() {
        var customers = repository.findAll();
        try {
            Thread.sleep(random.nextLong(5000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return customers;
    }

    @GetMapping("/customers/unreliable")
    public List<Customer> unreliable(@RequestParam(defaultValue = "30") int failureRate) {
        if (failureRate < 0 || failureRate > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "failureRate must be between 0 and 100");
        }
        if (random.nextInt(100) < failureRate) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated failure");
        }
        return repository.findAll();
    }
}
