package com.example.versioning.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides API version discovery for self-documenting API.
 * Clients can query this endpoint to discover available versions and their status.
 */
@RestController
@RequestMapping("/api")
public class VersionDiscoveryController {

    private final boolean v1Enabled;

    public VersionDiscoveryController(@Value("${api.v1.enabled:true}") boolean v1Enabled) {
        this.v1Enabled = v1Enabled;
    }

    @GetMapping("/versions")
    public Map<String, Object> getAvailableVersions() {
        var supported = new ArrayList<String>();
        if (v1Enabled) {
            supported.add("v1");
        }
        supported.add("v2");

        return Map.of(
                "current", "v2",
                "supported", supported,
                "deprecated", v1Enabled ? List.of("v1") : List.of(),
                "strategies", List.of("uri-path", "custom-header", "media-type", "query-parameter"),
                "v1", Map.of(
                        "status", v1Enabled ? "deprecated" : "disabled",
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
