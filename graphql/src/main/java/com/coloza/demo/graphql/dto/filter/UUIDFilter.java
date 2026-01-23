package com.coloza.demo.graphql.dto.filter;

import java.util.List;
import java.util.UUID;

public record UUIDFilter(
        UUID eq,
        List<UUID> in
) {
}
