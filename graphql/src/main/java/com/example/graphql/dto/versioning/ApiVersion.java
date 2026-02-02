package com.example.graphql.dto.versioning;

import java.util.List;

public record ApiVersion(
        String version,
        List<String> deprecatedFeatures,
        String supportedUntil
) {
}
