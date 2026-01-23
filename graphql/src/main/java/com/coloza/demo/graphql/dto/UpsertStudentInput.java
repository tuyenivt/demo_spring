package com.coloza.demo.graphql.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpsertStudentInput(
        UUID id,
        @NotBlank String name,
        String address,
        String dateOfBirth
) {
}
