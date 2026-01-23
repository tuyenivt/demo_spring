package com.coloza.demo.graphql.dto.pagination;

public record Edge<T>(
        T node,
        String cursor
) {
}
