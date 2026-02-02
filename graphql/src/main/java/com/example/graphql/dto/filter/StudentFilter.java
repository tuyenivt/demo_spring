package com.example.graphql.dto.filter;

public record StudentFilter(
        StringFilter name,
        StringFilter address,
        DateTimeFilter createdAt
) {
}
