package com.example.versioning.dto;

import com.fasterxml.jackson.annotation.JsonView;

import java.time.LocalDate;

public record EmployeeResponse(
        @JsonView(Views.V1.class)
        Long id,
        @JsonView(Views.V1.class)
        String name,
        @JsonView(Views.V1.class)
        String department,
        @JsonView(Views.V2.class)
        String title,
        @JsonView(Views.V2.class)
        LocalDate hireDate,
        @JsonView(Views.V2.class)
        EmployeeStatus status
) {
}
