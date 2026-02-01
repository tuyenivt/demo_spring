package com.example.versioning.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that centralizes API version detection.
 * Extracts version from Accept-version header or URI path and stores in request attribute.
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    public static final String API_VERSION_ATTRIBUTE = "apiVersion";
    public static final String DEFAULT_VERSION = "v2";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var version = request.getHeader("Accept-version");
        if (version == null) {
            version = extractVersionFromPath(request.getRequestURI());
        }
        request.setAttribute(API_VERSION_ATTRIBUTE, version != null ? version : DEFAULT_VERSION);
        return true;
    }

    private String extractVersionFromPath(String uri) {
        if (uri.contains("/v1/")) return "v1";
        if (uri.contains("/v2/")) return "v2";
        return null;
    }
}
