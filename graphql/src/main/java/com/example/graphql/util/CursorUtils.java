package com.example.graphql.util;

import com.example.graphql.exception.ErrorCode;
import com.example.graphql.exception.ValidationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class CursorUtils {
    private static final String CURSOR_PREFIX = "cursor:";

    private CursorUtils() {
    }

    public static String encode(UUID id) {
        if (id == null) return null;
        String raw = CURSOR_PREFIX + id.toString();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static UUID decode(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(cursor);
            String raw = new String(decoded, StandardCharsets.UTF_8);
            if (!raw.startsWith(CURSOR_PREFIX)) {
                throw new ValidationException(ErrorCode.INVALID_INPUT, "Invalid cursor format: missing prefix");
            }
            return UUID.fromString(raw.substring(CURSOR_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCode.INVALID_INPUT, "Invalid cursor format: " + e.getMessage());
        }
    }
}
