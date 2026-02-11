package com.example.ratelimiting.controller;

import com.example.ratelimiting.ratelimit.RateLimit;
import com.example.ratelimiting.ratelimit.RateLimits;
import com.example.ratelimiting.ratelimit.RefillStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, no rate limit!";
    }

    @RateLimit(profile = "strict")
    @GetMapping("/orders")
    public String orders() {
        return "Orders API - 5 requests per minute (strict profile, intervally refill)";
    }

    @RateLimit(profile = "standard")
    @GetMapping("/search")
    public String search() {
        return "Search API - 20 requests per minute (standard profile, greedy refill)";
    }

    @RateLimit(profile = "relaxed")
    @GetMapping("/reports")
    public String reports() {
        return "Reports API - 100 requests per hour (relaxed profile, intervally refill)";
    }

    /**
     * Demonstrates stacked limits: burst protection (10/s) + sustained cap (100/min).
     */
    @RateLimits({
            @RateLimit(limit = 10, durationSeconds = 1, strategy = RefillStrategy.GREEDY),
            @RateLimit(limit = 100, durationSeconds = 60)
    })
    @GetMapping("/submit")
    public String submit() {
        return "Submit API - stacked limits: 10/s burst + 100/min sustained";
    }
}
