package com.example.idempotent.controller;

import com.example.idempotent.dto.PaymentRequest;
import com.example.idempotent.dto.PaymentResponse;
import com.example.idempotent.idempotent.Idempotent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * Demo controller showcasing @Idempotent annotation for payment processing.
 * Prevents duplicate payment charges when network issues cause retries.
 */
@Slf4j
@RestController
@RequestMapping("/api/demo/payments")
public class PaymentDemoController {

    @PostMapping
    @Idempotent
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        log.info("Processing payment: amount={}, currency={}", request.getAmount(), request.getCurrency());

        // Simulate payment processing
        var response = PaymentResponse.builder()
                .transactionId(UUID.randomUUID().toString())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("COMPLETED")
                .processedAt(Instant.now())
                .build();

        log.info("Payment processed successfully: transactionId={}", response.getTransactionId());
        return ResponseEntity.ok(response);
    }
}
