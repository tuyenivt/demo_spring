package com.example.idempotent.idempotent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.idempotent")
public class IdempotentConfig {
    private int timeoutMinutes = 10;
    private int resultExpireMinutes = 1440; // 24h
    private String cacheStoreKey = "my-idempotent";
    private String clientHeaderKey = "Idempotent-Key";
    private String clientHeaderReplay = "Idempotent-Replay";
}
