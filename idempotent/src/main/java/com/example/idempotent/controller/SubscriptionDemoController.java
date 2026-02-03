package com.example.idempotent.controller;

import com.example.idempotent.dto.SubscribeRequest;
import com.example.idempotent.idempotent.PreventRepeatedRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo controller showcasing @PreventRepeatedRequests annotation for subscription.
 * Simple duplicate prevention for newsletter signup without full response caching.
 */
@Slf4j
@RestController
@RequestMapping("/api/demo/subscriptions")
public class SubscriptionDemoController {

    @PostMapping
    @PreventRepeatedRequests
    public ResponseEntity<Void> subscribe(@RequestBody SubscribeRequest request) {
        log.info("Processing subscription for: email={}", request.getEmail());

        // Simulate subscription processing
        log.info("Subscription processed successfully for: {}", request.getEmail());
        return ResponseEntity.ok().build();
    }
}
