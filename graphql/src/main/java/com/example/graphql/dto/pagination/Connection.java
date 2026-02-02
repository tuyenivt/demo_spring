package com.example.graphql.dto.pagination;

import java.util.List;

public record Connection<T>(
        List<Edge<T>> edges,
        PageInfoConnection pageInfo,
        long totalCount
) {
}
