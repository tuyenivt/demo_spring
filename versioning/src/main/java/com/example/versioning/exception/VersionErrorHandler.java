package com.example.versioning.exception;

import com.example.versioning.dto.ApiVersionError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class VersionErrorHandler {

    private static final List<String> SUPPORTED = List.of("v1", "v2");

    @ExceptionHandler(ApiVersionException.class)
    public ResponseEntity<ApiVersionError> handleApiVersionException(ApiVersionException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new ApiVersionError(
                exception.getMessage(),
                exception.getRequestedVersion(),
                SUPPORTED,
                "v2",
                "/api/versions"
        ));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiVersionError> handleMediaTypeNotAcceptable(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ApiVersionError(
                "Unsupported API version",
                request.getHeader("Accept"),
                SUPPORTED,
                "v2",
                "/api/versions"
        ));
    }
}
