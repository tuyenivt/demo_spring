package com.example.versioning.dto;

import java.util.List;

public record ApiVersionError(
        String error,
        String requestedVersion,
        List<String> supportedVersions,
        String currentVersion,
        String documentation
) {
}
