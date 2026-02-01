package com.example.versioning.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Provides API version discovery for self-documenting API.
 * Clients can query this endpoint to discover available versions and their status.
 */
@RestController
@RequestMapping("/api")
public class VersionDiscoveryController {

    @GetMapping("/versions")
    public Map<String, Object> getAvailableVersions() {
        return Map.of(
                "current", "v2",
                "supported", List.of("v1", "v2"),
                "deprecated", List.of("v1"),
                "v1", Map.of(
                        "status", "deprecated",
                        "sunset", "2025-12-31",
                        "docs", "/v1/docs"
                ),
                "v2", Map.of(
                        "status", "stable",
                        "docs", "/v2/docs"
                )
        );
    }
}
