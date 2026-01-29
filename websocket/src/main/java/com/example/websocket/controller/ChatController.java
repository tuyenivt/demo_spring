package com.example.websocket.controller;

import com.example.websocket.dto.ChatMessage;
import com.example.websocket.dto.ChatResponse;
import com.example.websocket.handler.MessageBroadcastHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import static com.example.websocket.constant.WebSocketDestinations.*;

/**
 * WebSocket message controller.
 * <p>
 * Handles incoming STOMP messages and coordinates responses.
 * Uses constructor injection for dependencies.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageBroadcastHandler broadcastHandler;

    /**
     * Handle chat messages sent to /app/chat.send
     *
     * @Payload automatically deserializes JSON to ChatMessage
     * Validates input and broadcasts to all subscribers
     * <p>
     * Threading: Executed on WebSocket message handling thread (non-blocking)
     */
    @MessageMapping(SEND_MESSAGE)
    public void handleChatMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("Received chat message from user: {}", message.username());

        // Defensive validation
        validateChatMessage(message);

        // Store username in session for connect/disconnect events
        storeUsernameInSession(message.username(), headerAccessor);

        // Extract session ID for logging/tracking
        var sessionId = headerAccessor.getSessionId();
        log.debug("Session ID: {}", sessionId);

        // Delegate to handler for broadcasting
        // This keeps controller thin and testable
        broadcastHandler.broadcastMessage(message);
    }

    /**
     * Handle private messages sent to /app/chat.private
     * <p>
     * Demonstrates point-to-point messaging.
     *
     * @SendToUser sends response only to the message sender.
     */
    @MessageMapping(SEND_PRIVATE_MESSAGE)
    @SendToUser(QUEUE_REPLY)
    public ChatResponse handlePrivateMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("Received private message from: {}", message.username());

        validateChatMessage(message);

        // Store username in session
        storeUsernameInSession(message.username(), headerAccessor);

        // Echo back to sender with confirmation
        return broadcastHandler.createPrivateResponse(message);
    }

    /**
     * Handle subscription to /topic/notifications
     * <p>
     * Sends a welcome notification when a client subscribes.
     * Demonstrates @SubscribeMapping usage.
     */
    @SubscribeMapping("/notifications")
    public ChatResponse handleNotificationSubscription(SimpMessageHeaderAccessor headerAccessor) {
        var sessionId = headerAccessor.getSessionId();
        log.info("Client subscribed to notifications: {}", sessionId);

        // Return immediate response to subscriber only
        return broadcastHandler.createWelcomeNotification();
    }

    /**
     * Store username in WebSocket session attributes.
     * <p>
     * This allows us to identify users during connect/disconnect events.
     */
    private void storeUsernameInSession(String username, SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getSessionAttributes() != null) {
            var existingUsername = (String) headerAccessor.getSessionAttributes().get("username");

            // Only store if not already present (first message from this user)
            if (existingUsername == null) {
                headerAccessor.getSessionAttributes().put("username", username);
                log.debug("Stored username '{}' in session", username);

                // Broadcast join notification
                var message = String.format(MSG_USER_JOINED_THE_CHAT, username);
                broadcastHandler.broadcastSystemNotification(message);
            }
        }
    }

    /**
     * Validate incoming chat message.
     *
     * @throws IllegalArgumentException if validation fails
     */
    private void validateChatMessage(ChatMessage message) {
        if (message.username() == null || message.username().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (message.content() == null || message.content().isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        if (message.content().length() > 1000) {
            throw new IllegalArgumentException("Message content too long (max 1000 characters)");
        }
    }
}
