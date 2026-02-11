package com.example.ratelimiting.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitProperties {

    private Map<String, Profile> profiles = new HashMap<>();

    @Data
    public static class Profile {
        private long limit;
        private long durationSeconds;
        private RefillStrategy strategy = RefillStrategy.INTERVALLY;
    }
}
