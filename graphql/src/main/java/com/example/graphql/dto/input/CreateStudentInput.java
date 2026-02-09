package com.example.graphql.dto.input;

import jakarta.validation.constraints.NotBlank;

public record CreateStudentInput(
        @NotBlank String name,
        String address,
        String dateOfBirth
) {
}
