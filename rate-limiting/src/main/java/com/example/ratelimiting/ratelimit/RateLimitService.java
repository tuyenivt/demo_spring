package com.example.ratelimiting.ratelimit;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final LettuceBasedProxyManager<String> proxyManager;

    public boolean tryConsume(String key, long limit, long durationSeconds) {
        var bucket = proxyManager.builder().build(key, () -> buildConfig(limit, durationSeconds));
        return bucket.tryConsume(1);
    }

    /**
     * Config max `limit` requests per `durationSeconds` seconds
     */
    private BucketConfiguration buildConfig(long limit, long durationSeconds) {
        return BucketConfiguration.builder()
                .addLimit(l -> l
                        .capacity(limit).refillIntervally(limit, Duration.ofSeconds(durationSeconds)))
                .build();
    }
}
