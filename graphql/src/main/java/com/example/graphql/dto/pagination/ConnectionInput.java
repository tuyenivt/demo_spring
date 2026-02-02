package com.example.graphql.dto.pagination;

public record ConnectionInput(
        Integer first,
        String after,
        Integer last,
        String before
) {
    public static final int DEFAULT_LIMIT = 20;

    public int getLimit() {
        if (first != null) return first;
        if (last != null) return last;
        return DEFAULT_LIMIT;
    }

    public boolean isForward() {
        return first != null || after != null || (last == null && before == null);
    }
}
