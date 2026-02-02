package com.example.ratelimiting.dto;

public record ErrorResponse(int status, String error, String message, long timestamp) {
}
