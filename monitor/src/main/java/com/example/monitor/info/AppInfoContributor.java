package com.example.monitor.info;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AppInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app", Map.of(
                "name", "demo-spring-monitor",
                "description", "Monitoring and observability demo",
                "java.version", System.getProperty("java.version")
        ));
    }
}
