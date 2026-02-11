package com.example.ratelimiting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldAllowRequestsWithinLimit() {
        var headers = new HttpHeaders();
        headers.set("X-USER-ID", "test-user-within-limit");
        var entity = new HttpEntity<>(headers);

        for (int i = 0; i < 5; i++) {
            var response = restTemplate.exchange("/api/orders", HttpMethod.GET, entity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().get("X-RateLimit-Limit")).contains("5");
            assertThat(response.getHeaders().get("X-RateLimit-Remaining")).isNotNull();
        }
    }

    @Test
    void shouldRejectRequestsExceedingLimit() {
        var headers = new HttpHeaders();
        headers.set("X-USER-ID", "rate-limit-user-exceeding");
        var entity = new HttpEntity<>(headers);

        // Exhaust the limit
        for (int i = 0; i < 5; i++) {
            restTemplate.exchange("/api/orders", HttpMethod.GET, entity, String.class);
        }

        // Next request should be rejected
        var response = restTemplate.exchange("/api/orders", HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getHeaders().get("Retry-After")).isNotNull();
    }

    @Test
    void shouldAllowAnonymousUsersWithIpBasedRateLimit() {
        var entity = new HttpEntity<>(new HttpHeaders());

        var response = restTemplate.exchange("/api/orders", HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get("X-RateLimit-Limit")).contains("5");
    }

    @Test
    void shouldReturnRateLimitHeaders() {
        var headers = new HttpHeaders();
        headers.set("X-USER-ID", "header-test-user");
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange("/api/orders", HttpMethod.GET, entity, String.class);

        assertThat(response.getHeaders().get("X-RateLimit-Limit")).contains("5");
        assertThat(response.getHeaders().get("X-RateLimit-Remaining")).isNotNull();
        assertThat(response.getHeaders().get("X-RateLimit-Reset")).isNotNull();
    }

    @Test
    void shouldAllowNoRateLimitEndpoint() {
        var entity = new HttpEntity<>(new HttpHeaders());

        for (int i = 0; i < 10; i++) {
            var response = restTemplate.exchange("/api/hello", HttpMethod.GET, entity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void shouldHandleConcurrentRequests() throws Exception {
        // Use a unique user per test run to avoid interference from other tests
        var userId = "concurrent-user-" + UUID.randomUUID();
        var headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);
        var entity = new HttpEntity<>(headers);

        int totalRequests = 20;
        var executor = Executors.newFixedThreadPool(totalRequests);
        List<CompletableFuture<ResponseEntity<String>>> futures = IntStream.range(0, totalRequests)
                .mapToObj(i -> CompletableFuture.supplyAsync(
                        () -> restTemplate.exchange("/api/orders", HttpMethod.GET, entity, String.class),
                        executor))
                .toList();

        var results = futures.stream().map(CompletableFuture::join).toList();
        executor.shutdown();

        long okCount = results.stream().filter(r -> r.getStatusCode() == HttpStatus.OK).count();
        long rejectedCount = results.stream().filter(r -> r.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS).count();

        assertThat(okCount).isEqualTo(5);
        assertThat(rejectedCount).isEqualTo(15);
    }

    @Test
    void shouldReturnStatusWithoutConsumingToken() {
        var headers = new HttpHeaders();
        headers.set("X-USER-ID", "status-check-user-" + UUID.randomUUID());
        var entity = new HttpEntity<>(headers);

        // First, consume one token via real endpoint
        restTemplate.exchange("/api/orders", HttpMethod.GET, entity, String.class);

        // Status endpoint should reflect remaining without consuming further
        var statusBefore = restTemplate.exchange("/api/rate-limit/status?profile=strict", HttpMethod.GET, entity, String.class);
        assertThat(statusBefore.getStatusCode()).isEqualTo(HttpStatus.OK);

        var statusAfter = restTemplate.exchange("/api/rate-limit/status?profile=strict", HttpMethod.GET, entity, String.class);
        assertThat(statusAfter.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Body should contain remaining and limit fields
        assertThat(statusBefore.getBody()).contains("remaining");
        assertThat(statusBefore.getBody()).contains("\"limit\":5");
    }
}
