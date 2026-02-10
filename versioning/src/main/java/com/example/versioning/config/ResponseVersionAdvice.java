package com.example.versioning.config;

import com.example.versioning.dto.ApiMeta;
import com.example.versioning.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@ControllerAdvice
public class ResponseVersionAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return body;
        }
        if (response instanceof ServletServerHttpResponse servletResponse
                && servletResponse.getServletResponse().getStatus() >= 400) {
            return body;
        }

        var path = servletRequest.getServletRequest().getRequestURI();
        if (!path.startsWith("/api/")) {
            return body;
        }
        if (body instanceof ApiResponse<?> || body instanceof byte[] || body instanceof CharSequence) {
            return body;
        }

        var apiVersion = String.valueOf(
                servletRequest.getServletRequest().getAttribute(ApiVersionInterceptor.API_VERSION_ATTRIBUTE)
        );
        var deprecation = "v1".equals(apiVersion) ? "2025-12-31" : null;
        var timestamp = Instant.now().truncatedTo(ChronoUnit.DAYS);

        return new ApiResponse<>(body, new ApiMeta(apiVersion, deprecation, timestamp));
    }
}
