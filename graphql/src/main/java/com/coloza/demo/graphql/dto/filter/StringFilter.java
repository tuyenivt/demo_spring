package com.coloza.demo.graphql.dto.filter;

import java.util.List;

public record StringFilter(
        String eq,
        String contains,
        String startsWith,
        String endsWith,
        List<String> in
) {
}
