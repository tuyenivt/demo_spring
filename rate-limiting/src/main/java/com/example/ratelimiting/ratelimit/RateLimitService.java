package com.example.ratelimiting.ratelimit;

import com.example.ratelimiting.dto.ConsumeResult;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final LettuceBasedProxyManager<String> proxyManager;

    public ConsumeResult tryConsumeWithInfo(String key, long limit, long durationSeconds) {
        var bucket = proxyManager.builder().build(key, () -> buildConfig(limit, durationSeconds));
        var probe = bucket.tryConsumeAndReturnRemaining(1);

        var resetTime = System.currentTimeMillis() / 1000 + durationSeconds;
        var retryAfter = probe.isConsumed() ? 0 : probe.getNanosToWaitForRefill() / 1_000_000_000;

        return new ConsumeResult(probe.isConsumed(), probe.getRemainingTokens(), resetTime, retryAfter);
    }

    private BucketConfiguration buildConfig(long limit, long durationSeconds) {
        return BucketConfiguration.builder()
                .addLimit(l -> l
                        .capacity(limit).refillIntervally(limit, Duration.ofSeconds(durationSeconds)))
                .build();
    }
}
