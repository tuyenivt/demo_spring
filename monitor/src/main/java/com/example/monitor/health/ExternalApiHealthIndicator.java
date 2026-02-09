package com.example.monitor.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component("externalApi")
public class ExternalApiHealthIndicator implements HealthIndicator {

    private final AtomicBoolean healthy = new AtomicBoolean(true);

    public void setHealthy(boolean value) {
        this.healthy.set(value);
    }

    public boolean isHealthy() {
        return healthy.get();
    }

    @Override
    public Health health() {
        if (healthy.get()) {
            return Health.up()
                    .withDetail("externalApi", "Available")
                    .build();
        }
        return Health.down()
                .withDetail("externalApi", "Unavailable")
                .withDetail("error", "Simulated outage")
                .build();
    }
}
