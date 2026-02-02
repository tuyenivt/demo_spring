package com.example.ratelimiting.exception;

import com.example.ratelimiting.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            var body = new ErrorResponse(
                    429,
                    "Too Many Requests",
                    "Rate limit exceeded. Please try again later.",
                    System.currentTimeMillis()
            );
            return ResponseEntity.status(429).body(body);
        }
        throw ex;
    }
}
