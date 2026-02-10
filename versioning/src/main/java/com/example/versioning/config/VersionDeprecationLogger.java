package com.example.versioning.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VersionDeprecationLogger {

    @Value("${api.v1.enabled:true}")
    private boolean v1Enabled;

    @PostConstruct
    public void logDeprecationWarning() {
        if (v1Enabled) {
            log.warn("Deprecated API version v1 is enabled. Consider disabling via api.v1.enabled=false.");
        }
    }
}
