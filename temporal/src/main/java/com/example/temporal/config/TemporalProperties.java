package com.example.temporal.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class TemporalProperties {
    @Value("${temporal.namespace}")
    private String namespace;
    @Value("${temporal.task-queue}")
    private String taskQueue;
    @Value("${temporal.target}")
    private String target;
}
