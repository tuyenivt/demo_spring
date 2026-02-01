package com.example.versioning.dto;

/**
 * V1 response - simpler structure for backward compatibility.
 */
public record EmployeeResponseV1(
        Long id,
        String name,
        String department
) {
}
