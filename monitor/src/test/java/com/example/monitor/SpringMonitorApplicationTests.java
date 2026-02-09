package com.example.monitor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringMonitorApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpointReturnsUp() {
        var response = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void readinessProbeIncludesDbAndExternalApi() {
        var response = restTemplate.getForEntity("/actuator/health/readiness", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"db\"");
        assertThat(response.getBody()).contains("\"externalApi\"");
    }

    @Test
    void prometheusOrMetricsEndpointContainsCustomMetrics() {
        restTemplate.getForEntity("/customers", String.class);
        restTemplate.getForEntity("/customers/transform", String.class);

        var prometheusResponse = restTemplate.getForEntity("/actuator/prometheus", String.class);
        if (prometheusResponse.getStatusCode() == HttpStatus.OK) {
            assertThat(prometheusResponse.getBody()).contains("customer_access_total");
            assertThat(prometheusResponse.getBody()).contains("customer_transform_seconds");
            assertThat(prometheusResponse.getBody()).contains("customer_total");
            return;
        }

        assertThat(prometheusResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var metricsResponse = restTemplate.getForEntity("/actuator/metrics", String.class);
        assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metricsResponse.getBody()).contains("customer.access");
        assertThat(metricsResponse.getBody()).contains("customer.transform");
        assertThat(metricsResponse.getBody()).contains("customer.total");
    }

    @Test
    void metricsEndpointListsExpectedMetrics() {
        var response = restTemplate.getForEntity("/actuator/metrics", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("customer.access");
        assertThat(response.getBody()).contains("customer.transform");
        assertThat(response.getBody()).contains("db.query");
    }
}