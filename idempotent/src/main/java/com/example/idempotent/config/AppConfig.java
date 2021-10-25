package com.example.idempotent.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.redis.ttl}")
    private Long redisCacheTtl;
}
