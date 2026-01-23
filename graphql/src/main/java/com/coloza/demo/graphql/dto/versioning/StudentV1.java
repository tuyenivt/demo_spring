package com.coloza.demo.graphql.dto.versioning;

import com.coloza.demo.graphql.entity.Student;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StudentV1(
        UUID id,
        String name,
        String address,
        String dateOfBirth,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static StudentV1 from(Student student) {
        return new StudentV1(
                student.getId(),
                student.getName(),
                student.getAddress(),
                student.getDateOfBirth() != null ? student.getDateOfBirth().toString() : null,
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}
