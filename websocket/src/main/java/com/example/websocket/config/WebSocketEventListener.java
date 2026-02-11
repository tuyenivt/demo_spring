package com.example.websocket.config;

import com.example.websocket.handler.MessageBroadcastHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

import static com.example.websocket.constant.WebSocketDestinations.MSG_USER_JOINED;
import static com.example.websocket.constant.WebSocketDestinations.MSG_USER_LEFT;
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
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        var sessionId = event.getMessage().getHeaders().get("simpSessionId");
        var username = extractUsername(event.getUser());

        log.info("New WebSocket connection - Session ID: {}, User: {}", sessionId, username);
        broadcastHandler.broadcastSystemNotification(String.format(MSG_USER_JOINED, username));
    }

    /**
     * Handle session disconnect events.
     * <p>
     * Triggered when a client disconnects (closes browser, network failure, etc.)
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        var sessionId = event.getSessionId();
        var username = extractUsername(event.getUser());

        log.info("WebSocket disconnection - Session ID: {}, User: {}, CloseStatus: {}", sessionId, username, event.getCloseStatus());
        broadcastHandler.broadcastSystemNotification(String.format(MSG_USER_LEFT, username));
    }

    /**
     * Extract username from authenticated principal.
     *
     * @return username or null if not found
     */
    private String extractUsername(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return UNKNOWN_SENDER;
        }
        return principal.getName();
    }
}
