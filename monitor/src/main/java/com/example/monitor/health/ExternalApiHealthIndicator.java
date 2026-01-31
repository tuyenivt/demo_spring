package com.example.monitor.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ExternalApiHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        if (checkExternalApi()) {
            return Health.up()
                    .withDetail("externalApi", "Available")
                    .withDetail("responseTime", "< 100ms")
                    .build();
        }
        return Health.down()
                .withDetail("externalApi", "Unavailable")
                .withDetail("error", "Connection timeout")
                .build();
    }

    private boolean checkExternalApi() {
        // Simulate checking external service availability
        // In a real application, this would make an HTTP call or ping the service
        return true;
    }
}
