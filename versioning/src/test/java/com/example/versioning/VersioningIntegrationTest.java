package com.example.versioning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VersioningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // URI Path Versioning Tests

    @Test
    void shouldRouteToV2ByPath() throws Exception {
        mockMvc.perform(get("/v2/schedule"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("v2")));
    }

    // Header Versioning Tests

    @Test
    void shouldRouteToV2ByHeader() throws Exception {
        mockMvc.perform(get("/location")
                        .header("Accept-version", "v2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("v2")));
    }

    @Test
    void shouldReturn404ForMissingVersionHeader() throws Exception {
        mockMvc.perform(get("/location"))
                .andExpect(status().isNotFound());
    }

    // Media Type Versioning Tests

    @Test
    void shouldReturnProductV1WithMediaType() throws Exception {
        mockMvc.perform(get("/api/products")
                        .accept("application/vnd.company.v1+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void shouldReturnProductV2WithMediaType() throws Exception {
        mockMvc.perform(get("/api/products")
                        .accept("application/vnd.company.v2+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.description").value("Premium quality widget"))
                .andExpect(jsonPath("$.sku").value("WIDGET-001"));
    }

    // Deprecation Headers Tests

    @Test
    void shouldIncludeDeprecationHeadersForV1() throws Exception {
        mockMvc.perform(get("/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("Sunset", "Sat, 31 Dec 2025 23:59:59 GMT"))
                .andExpect(header().string("Link", "</v2/employees>; rel=\"successor-version\""));
    }

    // Version Discovery Tests

    @Test
    void shouldReturnVersionDiscoveryInfo() throws Exception {
        mockMvc.perform(get("/api/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current").value("v2"))
                .andExpect(jsonPath("$.supported", hasSize(2)))
                .andExpect(jsonPath("$.deprecated", hasSize(1)));
    }

    // V2 Employee Controller Tests

    @Test
    void shouldReturnV2EmployeeResponse() throws Exception {
        mockMvc.perform(get("/api/v2/employees"))
                .andExpect(status().isOk());
    }
}
