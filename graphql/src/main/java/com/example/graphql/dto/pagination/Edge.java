package com.example.graphql.dto.pagination;

public record Edge<T>(
        T node,
        String cursor
) {
}
