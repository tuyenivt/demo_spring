package com.example.graphql.dto;

import java.util.UUID;

public record UpdateStudentInput(
        UUID id,
        String name,
        String address,
        String dateOfBirth
) {
}
