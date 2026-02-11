package com.example.ratelimiting.ratelimit;

import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    LettuceBasedProxyManager<String> proxyManager;

    RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService(proxyManager);
    }

    @Test
    void shouldReturnConsumedTrueWhenTokenAvailable() {
        var probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(4L);
        when(probe.getNanosToWaitForRefill()).thenReturn(0L);

        var bucket = mock(BucketProxy.class);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        var builder = mock(RemoteBucketBuilder.class);
        when(builder.build(eq("key"), any(Supplier.class))).thenReturn(bucket);
        when(proxyManager.builder()).thenReturn(builder);

        var result = rateLimitService.tryConsumeWithInfo("key", 5, 60, RefillStrategy.INTERVALLY);

        assertThat(result.consumed()).isTrue();
        assertThat(result.remainingTokens()).isEqualTo(4);
        assertThat(result.retryAfterSeconds()).isEqualTo(0);
    }

    @Test
    void shouldReturnConsumedFalseWhenBucketExhausted() {
        var probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getRemainingTokens()).thenReturn(0L);
        when(probe.getNanosToWaitForRefill()).thenReturn(30_000_000_000L); // 30 seconds

        var bucket = mock(BucketProxy.class);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        var builder = mock(RemoteBucketBuilder.class);
        when(builder.build(eq("key"), any(Supplier.class))).thenReturn(bucket);
        when(proxyManager.builder()).thenReturn(builder);

        var result = rateLimitService.tryConsumeWithInfo("key", 5, 60, RefillStrategy.INTERVALLY);

        assertThat(result.consumed()).isFalse();
        assertThat(result.remainingTokens()).isEqualTo(0);
        assertThat(result.retryAfterSeconds()).isEqualTo(30);
    }

    @Test
    void shouldComputeResetTimeFromProbeNanos() {
        var nanosToReset = 60_000_000_000L; // 60 seconds
        var probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(4L);
        when(probe.getNanosToWaitForRefill()).thenReturn(nanosToReset);

        var bucket = mock(BucketProxy.class);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        var builder = mock(RemoteBucketBuilder.class);
        when(builder.build(eq("key"), any(Supplier.class))).thenReturn(bucket);
        when(proxyManager.builder()).thenReturn(builder);

        var before = System.currentTimeMillis() / 1000;
        var result = rateLimitService.tryConsumeWithInfo("key", 5, 60, RefillStrategy.INTERVALLY);
        var after = System.currentTimeMillis() / 1000;

        assertThat(result.resetTimeSeconds()).isBetween(before + 60, after + 60);
    }

    @Test
    void shouldReturnAvailableTokens() {
        var bucket = mock(BucketProxy.class);
        when(bucket.getAvailableTokens()).thenReturn(3L);

        var builder = mock(RemoteBucketBuilder.class);
        when(builder.build(eq("key"), any(Supplier.class))).thenReturn(bucket);
        when(proxyManager.builder()).thenReturn(builder);

        var available = rateLimitService.getAvailableTokens("key", 5, 60, RefillStrategy.INTERVALLY);

        assertThat(available).isEqualTo(3);
    }
}
