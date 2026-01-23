package com.coloza.demo.graphql.dto.filter;

import java.time.OffsetDateTime;

public record DateTimeFilter(
        OffsetDateTime eq,
        OffsetDateTime gt,
        OffsetDateTime gte,
        OffsetDateTime lt,
        OffsetDateTime lte,
        DateTimeRange between
) {
}
