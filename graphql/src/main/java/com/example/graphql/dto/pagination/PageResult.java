package com.example.graphql.dto.pagination;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        PageInfoDto pageInfo
) {
    public static <T> PageResult<T> from(org.springframework.data.domain.Page<T> page) {
        return new PageResult<>(
                page.getContent(),
                PageInfoDto.from(page)
        );
    }
}
