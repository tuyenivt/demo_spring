package com.example.ratelimiting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

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
}
