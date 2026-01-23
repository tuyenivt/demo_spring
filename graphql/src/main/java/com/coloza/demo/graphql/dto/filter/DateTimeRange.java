package com.coloza.demo.graphql.dto.filter;

import java.time.OffsetDateTime;

public record DateTimeRange(
        OffsetDateTime start,
        OffsetDateTime end
) {
}
