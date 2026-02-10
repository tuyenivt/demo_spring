package com.example.versioning.config;

import com.example.versioning.metrics.VersionUsageMetrics;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that centralizes API version detection.
 * Extracts version from query parameter, Accept-version header, media type, or URI path.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiVersionInterceptor implements HandlerInterceptor {

    public static final String API_VERSION_ATTRIBUTE = "apiVersion";
    public static final String REQUEST_START_NANOS = "requestStartNanos";
    public static final String DEFAULT_VERSION = "v2";

    private final VersionUsageMetrics versionUsageMetrics;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var version = extractVersionFromQuery(request);
        if (!StringUtils.hasText(version)) {
            version = extractVersionFromHeader(request);
        }
        if (!StringUtils.hasText(version)) {
            version = extractVersionFromMediaType(request);
        }
        if (!StringUtils.hasText(version)) {
            version = extractVersionFromPath(request.getRequestURI());
        }
        request.setAttribute(REQUEST_START_NANOS, System.nanoTime());
        request.setAttribute(API_VERSION_ATTRIBUTE, version != null ? version : DEFAULT_VERSION);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        var version = String.valueOf(request.getAttribute(API_VERSION_ATTRIBUTE));
        var start = request.getAttribute(REQUEST_START_NANOS);
        var elapsedMillis = 0L;
        if (start instanceof Long startNanos) {
            elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
        }

        versionUsageMetrics.record(version, request.getMethod(), String.valueOf(response.getStatus()));
        log.info(
                "apiVersionRequest method={} uri={} version={} status={} durationMs={}",
                request.getMethod(),
                request.getRequestURI(),
                version,
                response.getStatus(),
                elapsedMillis
        );
    }

    private String extractVersionFromQuery(HttpServletRequest request) {
        var raw = request.getParameter("version");
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return raw.startsWith("v") ? raw : "v" + raw;
    }

    private String extractVersionFromHeader(HttpServletRequest request) {
        var version = request.getHeader("Accept-version");
        if (!StringUtils.hasText(version)) {
            return null;
        }
        return version.startsWith("v") ? version : "v" + version;
    }

    private String extractVersionFromMediaType(HttpServletRequest request) {
        var accept = request.getHeader("Accept");
        if (!StringUtils.hasText(accept)) {
            return null;
        }
        if (accept.contains("application/vnd.company.v1+json")) return "v1";
        if (accept.contains("application/vnd.company.v2+json")) return "v2";
        return null;
    }

    private String extractVersionFromPath(String uri) {
        if (uri.contains("/v1/")) return "v1";
        if (uri.contains("/v2/")) return "v2";
        if (uri.contains("/api/v1/")) return "v1";
        if (uri.contains("/api/v2/")) return "v2";
        return null;
    }
}
