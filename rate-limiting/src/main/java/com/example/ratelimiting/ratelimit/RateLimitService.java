package com.example.ratelimiting.ratelimit;

import com.example.ratelimiting.dto.ConsumeResult;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final LettuceBasedProxyManager<String> proxyManager;

    public ConsumeResult tryConsumeWithInfo(String key, long limit, long durationSeconds, RefillStrategy strategy) {
        var bucket = proxyManager.builder().build(key, () -> buildConfig(limit, durationSeconds, strategy));
        var probe = bucket.tryConsumeAndReturnRemaining(1);

        var nanosToReset = probe.getNanosToWaitForRefill();
        var resetTime = System.currentTimeMillis() / 1000 + (nanosToReset / 1_000_000_000);
        var retryAfter = probe.isConsumed() ? 0 : nanosToReset / 1_000_000_000;

        log.debug("Rate limit check: key={}, remaining={}, consumed={}", key, probe.getRemainingTokens(), probe.isConsumed());

        return new ConsumeResult(probe.isConsumed(), probe.getRemainingTokens(), resetTime, retryAfter);
    }

    public long getAvailableTokens(String key, long limit, long durationSeconds, RefillStrategy strategy) {
        var bucket = proxyManager.builder().build(key, () -> buildConfig(limit, durationSeconds, strategy));
        return bucket.getAvailableTokens();
    }

    private BucketConfiguration buildConfig(long limit, long durationSeconds, RefillStrategy strategy) {
        return BucketConfiguration.builder()
                .addLimit(l -> {
                    var base = l.capacity(limit);
                    return strategy == RefillStrategy.GREEDY
                            ? base.refillGreedy(limit, Duration.ofSeconds(durationSeconds))
                            : base.refillIntervally(limit, Duration.ofSeconds(durationSeconds));
                })
                .build();
    }
}
