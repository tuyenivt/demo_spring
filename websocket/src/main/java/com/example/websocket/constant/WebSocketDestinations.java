package com.example.websocket.constant;

/**
 * Centralized WebSocket destination paths.
 * <p>
 * This prevents magic strings throughout the codebase and makes
 * destination management easier.
 */
public final class WebSocketDestinations {

    private WebSocketDestinations() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    // WebSocket endpoint for client connections
    public static final String WEBSOCKET_ENDPOINT = "/ws";

    // Destination prefixes
    public static final String APP_DESTINATION_PREFIX = "/app";
    public static final String TOPIC_DESTINATION_PREFIX = "/topic";
    public static final String QUEUE_DESTINATION_PREFIX = "/queue";
}
