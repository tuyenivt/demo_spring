package com.example.monitor.endpoint;

import com.example.monitor.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@Endpoint(id = "app")
@RequiredArgsConstructor
public class AppEndpoint {

    private final CustomerRepository repository;

    @ReadOperation
    public Map<String, Object> appInfo() {
        return Map.of(
                "customerCount", repository.count(),
                "timestamp", Instant.now().toString()
        );
    }
}
