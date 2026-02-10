package com.example.versioning.controller;

import com.example.versioning.dto.ReportV1;
import com.example.versioning.dto.ReportV2;
import com.example.versioning.exception.ApiVersionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @GetMapping
    public Object getReport(@RequestParam(name = "version", defaultValue = "2") String version) {
        return switch (version) {
            case "1", "v1" -> new ReportV1(1001L, "Weekly Status", "v1 compact report format");
            case "2", "v2" -> new ReportV2(
                    1001L,
                    "Weekly Status",
                    "v2 detailed report format",
                    "Platform Team",
                    Instant.parse("2026-02-10T00:00:00Z"),
                    List.of("ops", "status", "weekly")
            );
            default -> throw new ApiVersionException("Unsupported API version", version, HttpStatus.BAD_REQUEST);
        };
    }
}
