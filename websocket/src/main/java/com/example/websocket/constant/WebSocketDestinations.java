package com.example.websocket.constant;

/**
 * Centralized WebSocket destination paths and message constants.
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

    // Application destinations (client sends to these)
    public static final String SEND_MESSAGE = "/chat.send";
    public static final String SEND_PRIVATE_MESSAGE = "/chat.private";
    public static final String SUBSCRIBE_HISTORY = "/history";

    // Broker destinations (server broadcasts to these)
    public static final String TOPIC_MESSAGES = "/topic/messages";
    public static final String TOPIC_NOTIFICATIONS = "/topic/notifications";

    // User-specific queue destinations
    public static final String QUEUE_PRIVATE = "/queue/private";
    public static final String QUEUE_ERRORS = "/queue/errors";

    // System sender name
    public static final String SYSTEM_SENDER = "System";
    public static final String UNKNOWN_SENDER = "Unknown";

    // Message types
    public static final String MESSAGE_TYPE_BROADCAST = "broadcast";
    public static final String MESSAGE_TYPE_PRIVATE = "private";
    public static final String MESSAGE_TYPE_NOTIFICATION = "notification";

    // System messages
    public static final String MSG_WELCOME_NOTIFICATION = "Welcome! You are now subscribed to notifications.";
    public static final String MSG_NEW_MESSAGE_FROM = "New message from %s";
    public static final String MSG_USER_JOINED = "User '%s' joined the chat";
    public static final String MSG_USER_LEFT = "User '%s' left the chat";
    public static final String MSG_SERVER_SHUTTING_DOWN = "Server is shutting down. Please reconnect shortly.";
}
