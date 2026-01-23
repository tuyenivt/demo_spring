package com.coloza.demo.graphql.dto.pagination;

public record PageInfoConnection(
        boolean hasNextPage,
        boolean hasPreviousPage,
        String startCursor,
        String endCursor
) {
}
