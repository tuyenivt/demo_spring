package com.example.ratelimiting.ratelimit;

import com.example.ratelimiting.dto.ConsumeResult;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    RateLimitService rateLimitService;
    @Mock
    RateLimitProperties rateLimitProperties;
    @Mock
    UserContext userContext;
    @Mock
    HttpServletResponse response;
    @Mock
    JoinPoint joinPoint;
    @Mock
    MethodSignature methodSignature;

    RateLimitAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new RateLimitAspect(rateLimitService, rateLimitProperties, userContext, response);
        when(userContext.getIdentifier()).thenReturn("user:test");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.toShortString()).thenReturn("HomeController.orders()");
    }

    @Test
    void shouldSetHeadersAndAllowRequestWhenTokenAvailable() throws NoSuchMethodException {
        var method = AnnotatedController.class.getMethod("limited");
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new AnnotatedController());

        var consumeResult = new ConsumeResult(true, 4L, 9999L, 0L);
        when(rateLimitService.tryConsumeWithInfo(anyString(), eq(5L), eq(60L), eq(RefillStrategy.INTERVALLY)))
                .thenReturn(consumeResult);

        aspect.rateLimit(joinPoint);

        verify(response).setHeader("X-RateLimit-Limit", "5");
        verify(response).setHeader("X-RateLimit-Remaining", "4");
        verify(response).setHeader("X-RateLimit-Reset", "9999");
        verify(response, never()).setHeader(eq("Retry-After"), anyString());
    }

    @Test
    void shouldThrow429WhenLimitExceeded() throws NoSuchMethodException {
        var method = AnnotatedController.class.getMethod("limited");
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new AnnotatedController());

        var consumeResult = new ConsumeResult(false, 0L, 9999L, 30L);
        when(rateLimitService.tryConsumeWithInfo(anyString(), eq(5L), eq(60L), eq(RefillStrategy.INTERVALLY)))
                .thenReturn(consumeResult);

        assertThatThrownBy(() -> aspect.rateLimit(joinPoint))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        verify(response).setHeader("Retry-After", "30");
    }

    @Test
    void shouldAllowRequestWhenRedisUnavailable() throws NoSuchMethodException {
        var method = AnnotatedController.class.getMethod("limited");
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new AnnotatedController());

        when(rateLimitService.tryConsumeWithInfo(anyString(), anyLong(), anyLong(), any()))
                .thenThrow(new RuntimeException("Redis connection refused"));

        // Should not throw — fail-open behaviour
        aspect.rateLimit(joinPoint);
    }

    @Test
    void shouldResolveProfileFromProperties() throws NoSuchMethodException {
        var method = AnnotatedController.class.getMethod("profileLimited");
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new AnnotatedController());

        var profile = new RateLimitProperties.Profile();
        profile.setLimit(10);
        profile.setDurationSeconds(30);
        profile.setStrategy(RefillStrategy.GREEDY);
        when(rateLimitProperties.getProfiles()).thenReturn(Map.of("custom", profile));

        var consumeResult = new ConsumeResult(true, 9L, 9999L, 0L);
        when(rateLimitService.tryConsumeWithInfo(anyString(), eq(10L), eq(30L), eq(RefillStrategy.GREEDY)))
                .thenReturn(consumeResult);

        aspect.rateLimit(joinPoint);

        verify(response).setHeader("X-RateLimit-Limit", "10");
    }

    @Test
    void shouldApplyClassLevelRateLimit() throws NoSuchMethodException {
        var method = ClassLevelController.class.getMethod("endpoint");
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new ClassLevelController());

        var consumeResult = new ConsumeResult(true, 2L, 9999L, 0L);
        when(rateLimitService.tryConsumeWithInfo(anyString(), eq(3L), eq(10L), eq(RefillStrategy.INTERVALLY)))
                .thenReturn(consumeResult);

        aspect.rateLimit(joinPoint);

        verify(response).setHeader("X-RateLimit-Limit", "3");
    }

    // --- helper test doubles ---

    static class AnnotatedController {
        @RateLimit(limit = 5, durationSeconds = 60)
        public void limited() {
        }

        @RateLimit(profile = "custom")
        public void profileLimited() {
        }
    }

    @RateLimit(limit = 3, durationSeconds = 10)
    static class ClassLevelController {
        public void endpoint() {
        }
    }
}
