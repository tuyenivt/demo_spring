package com.example.versioning.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Endpoint(id = "api-versions")
public class ApiVersionsEndpoint {

    private final VersionUsageMetrics versionUsageMetrics;

    @ReadOperation
    public Map<String, VersionUsageSnapshot> versions() {
        return versionUsageMetrics.snapshots();
    }
}
