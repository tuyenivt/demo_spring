package com.example.websocket.config;

import com.example.websocket.handler.MessageBroadcastHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static com.example.websocket.constant.WebSocketDestinations.UNKNOWN_SENDER;

/**
 * Listener for WebSocket session lifecycle events.
 * <p>
 * Broadcasts notifications when users connect/disconnect.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final MessageBroadcastHandler broadcastHandler;

    /**
     * Handle session connect events.
     * <p>
     * Triggered when a client successfully establishes a WebSocket connection.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        var headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        var sessionId = headerAccessor.getSessionId();

        log.info("New WebSocket connection - Session ID: {}", sessionId);

        // Get username from session attributes if available
        var username = getUsernameFromSession(headerAccessor);

        if (username != null) {
            var message = String.format("User '%s' joined the chat", username);
            broadcastHandler.broadcastSystemNotification(message);
            log.info("User connected: {}", username);
        }
    }

    /**
     * Handle session disconnect events.
     * <p>
     * Triggered when a client disconnects (closes browser, network failure, etc.)
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        var headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        var sessionId = headerAccessor.getSessionId();

        log.info("WebSocket disconnection - Session ID: {}", sessionId);

        // Get username from session attributes
        var username = getUsernameFromSession(headerAccessor);

        if (username != null) {
            var message = String.format("User '%s' left the chat", username);
            broadcastHandler.broadcastSystemNotification(message);
            log.info("User disconnected: {}", username);
        }
    }

    /**
     * Extract username from session attributes.
     *
     * @return username or null if not found
     */
    private String getUsernameFromSession(StompHeaderAccessor headerAccessor) {
        var sessionAttributes = headerAccessor.getSessionAttributes();
        return sessionAttributes == null ? UNKNOWN_SENDER : (String) sessionAttributes.get("username");
    }
}
