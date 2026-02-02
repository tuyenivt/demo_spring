package com.example.ratelimiting.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter
@Component
@RequestScope
public class UserContext {

    private final String identifier;

    public UserContext(HttpServletRequest request) {
        var userId = request.getHeader("X-USER-ID");
        if (userId != null && !userId.isBlank()) {
            this.identifier = "user:" + userId;
        } else {
            this.identifier = "ip:" + getClientIp(request);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        var forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

//    public String getUserIdFromSecurityContext() {
//        var authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication instanceof UserDetails userDetails) {
//            return userDetails.getUsername();
//        }
//        throw new IllegalStateException("Unrecognized User Identifier");
//    }
}
