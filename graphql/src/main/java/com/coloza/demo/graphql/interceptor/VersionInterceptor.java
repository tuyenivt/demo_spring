package com.coloza.demo.graphql.interceptor;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class VersionInterceptor implements WebGraphQlInterceptor {

    public static final String API_VERSION_HEADER = "X-API-Version";
    public static final String API_VERSION_CONTEXT_KEY = "apiVersion";
    public static final String DEFAULT_VERSION = "2";

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String version = request.getHeaders().getFirst(API_VERSION_HEADER);
        if (version == null || version.isBlank()) {
            version = DEFAULT_VERSION;
        }

        String finalVersion = version;
        request.configureExecutionInput((executionInput, builder) ->
                builder.graphQLContext(context -> context.put(API_VERSION_CONTEXT_KEY, finalVersion))
                        .build()
        );

        return chain.next(request);
    }
}
