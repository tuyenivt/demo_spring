package com.example.graphql.dto.pagination;

public record PageInfoDto(
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
    public static PageInfoDto from(org.springframework.data.domain.Page<?> page) {
        return new PageInfoDto(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
