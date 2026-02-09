package com.example.graphql.dto.pagination;

public record ConnectionInput(
        Integer first,
        String after,
        Integer last,
        String before
) {
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public int getLimit() {
        int limit;
        if (first != null) {
            limit = first;
        } else if (last != null) {
            limit = last;
        } else {
            limit = DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    public boolean isForward() {
        return first != null || after != null || (last == null && before == null);
    }
}
