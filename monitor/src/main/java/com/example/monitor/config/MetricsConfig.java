package com.example.monitor.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    Counter customerAccessCounter(MeterRegistry registry) {
        return Counter.builder("customer.access")
                .description("Number of customer list accesses")
                .register(registry);
    }

    @Bean
    Timer customerTransformTimer(MeterRegistry registry) {
        return Timer.builder("customer.transform")
                .description("Customer transform operation timing")
                .register(registry);
    }
}
