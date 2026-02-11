package com.example.ratelimiting.controller;

import com.example.ratelimiting.ratelimit.RateLimitProperties;
import com.example.ratelimiting.ratelimit.RateLimitService;
import com.example.ratelimiting.ratelimit.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rate-limit")
@RequiredArgsConstructor
public class RateLimitStatusController {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;
    private final UserContext userContext;

    /**
     * Returns remaining token count for a profile without consuming a token.
     * <p>
     * Example: GET /api/rate-limit/status?profile=strict
     */
    @GetMapping("/status")
    public Map<String, Object> status(@RequestParam String profile) {
        var profileConfig = rateLimitProperties.getProfiles().get(profile);
        if (profileConfig == null) {
            throw new IllegalArgumentException("Unknown rate-limit profile: " + profile);
        }

        var key = String.join(":", "rate-limit", userContext.getIdentifier(), "status", profile);
        var available = rateLimitService.getAvailableTokens(
                key, profileConfig.getLimit(), profileConfig.getDurationSeconds(), profileConfig.getStrategy());

        return Map.of(
                "profile", profile,
                "limit", profileConfig.getLimit(),
                "remaining", available,
                "durationSeconds", profileConfig.getDurationSeconds()
        );
    }
}
