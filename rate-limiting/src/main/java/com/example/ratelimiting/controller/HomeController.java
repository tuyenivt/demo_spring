package com.example.ratelimiting.controller;

import com.example.ratelimiting.ratelimit.RateLimit;
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

    @RateLimit(limit = 5, durationSeconds = 60)
    @GetMapping("/orders")
    public String orders() {
        return "Orders API - 5 requests per minute";
    }

    @RateLimit(limit = 20, durationSeconds = 60)
    @GetMapping("/search")
    public String search() {
        return "Search API - 20 requests per minute";
    }

    @RateLimit(limit = 100, durationSeconds = 3600)
    @GetMapping("/reports")
    public String reports() {
        return "Reports API - 100 requests per hour";
    }
}
