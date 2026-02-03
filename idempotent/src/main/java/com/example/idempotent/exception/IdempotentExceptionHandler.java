package com.example.idempotent.exception;

import com.example.idempotent.dto.ErrorResponse;
import com.example.idempotent.idempotent.IdempotentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class IdempotentExceptionHandler {

    @ExceptionHandler(IdempotentException.class)
    public ResponseEntity<ErrorResponse> handleIdempotentException(IdempotentException ex) {
        log.warn("Duplicate request detected: {}", ex.getMessage());

        var error = ErrorResponse.builder()
                .code("DUPLICATE_REQUEST")
                .message("Request already processed or in progress")
                .detail(ex.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
