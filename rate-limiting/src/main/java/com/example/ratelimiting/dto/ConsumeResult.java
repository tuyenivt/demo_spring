package com.example.ratelimiting.dto;

public record ConsumeResult(boolean consumed, long remainingTokens, long resetTimeSeconds, long retryAfterSeconds) {
}
