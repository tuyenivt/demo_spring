package com.example.versioning.dto;

import java.time.LocalDate;

/**
 * V2 response - richer structure with additional fields.
 */
public record EmployeeResponseV2(
        Long id,
        String name,
        String title,
        String department,
        LocalDate hireDate,
        EmployeeStatus status
) {
}
