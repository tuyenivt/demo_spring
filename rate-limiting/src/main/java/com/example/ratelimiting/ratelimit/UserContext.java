package com.example.ratelimiting.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class UserContext {

    private final String userId;

    public UserContext(HttpServletRequest request) {
        this.userId = request.getHeader("X-USER-ID");
    }

    public String getUserId() {
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Unrecognized User Identifier");
        }
        return userId;
    }
}
