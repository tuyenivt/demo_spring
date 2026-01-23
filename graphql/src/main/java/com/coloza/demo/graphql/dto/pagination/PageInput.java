package com.coloza.demo.graphql.dto.pagination;

public record PageInput(
        Integer page,
        Integer size
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;

    public int getPageOrDefault() {
        return page != null ? page : DEFAULT_PAGE;
    }

    public int getSizeOrDefault() {
        return size != null ? size : DEFAULT_SIZE;
    }
}
