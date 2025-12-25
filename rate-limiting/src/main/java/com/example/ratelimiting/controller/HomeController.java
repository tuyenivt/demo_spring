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
        return "Hello, no rate limit api!";
    }

    @RateLimit(limit = 5, durationSeconds = 60)
    @GetMapping("/orders")
    public String orders() {
        return "Rate limited api (Orders API))";
    }
}
