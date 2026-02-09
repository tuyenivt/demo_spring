package com.example.graphql.dto.pagination;

public record PageInput(
        Integer page,
        Integer size
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public int getPageOrDefault() {
        return page != null ? page : DEFAULT_PAGE;
    }

    public int getSizeOrDefault() {
        int s = size != null ? size : DEFAULT_SIZE;
        return Math.min(s, MAX_SIZE);
    }
}
