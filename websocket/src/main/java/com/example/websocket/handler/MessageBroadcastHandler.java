package com.example.websocket.handler;

import com.example.websocket.dto.ChatMessage;
import com.example.websocket.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.example.websocket.constant.WebSocketDestinations.*;

/**
 * Handles message broadcasting logic.
 * <p>
 * Separates broadcasting concerns from controller layer.
 * Uses SimpMessagingTemplate to send messages to destinations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroadcastHandler {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast message to all subscribers of /topic/messages
     * <p>
     * Threading note: This is non-blocking. SimpMessagingTemplate
     * handles message queuing and delivery asynchronously.
     */
    public void broadcastMessage(ChatMessage message) {
        var response = new ChatResponse(message.username(), message.content(), Instant.now(), MESSAGE_TYPE_BROADCAST);

        log.info("Broadcasting message to {}: {}", TOPIC_MESSAGES, response);

        // Send to topic - all subscribers receive this
        messagingTemplate.convertAndSend(TOPIC_MESSAGES, response);

        // Also send a notification about the new message
        sendNewMessageNotification(message.username());
    }

    /**
     * Send private message to specific user.
     * <p>
     * Uses convertAndSendToUser to route message to user-specific queue.
     * Spring automatically prefixes with /user/{username}
     */
    public void sendPrivateMessageToUser(ChatMessage message) {
        var response = new ChatResponse(message.username(), message.content(), Instant.now(), MESSAGE_TYPE_PRIVATE);

        log.info("Sending private message from {} to {}", message.username(), message.targetUsername());

        // IMPORTANT: Spring adds /user/{username} prefix automatically
        // So QUEUE_PRIVATE should be just "/queue/private" without /user prefix
        // Client subscribes to: /user/queue/private
        // Server sends to: convertAndSendToUser(username, "/queue/private", msg)
        // Actual destination becomes: /user/{username}/queue/private
        messagingTemplate.convertAndSendToUser(message.targetUsername(), QUEUE_PRIVATE, response);

        log.info("Private message sent to destination: /user/{}/queue/private", message.targetUsername());
    }

    /**
     * Create welcome notification for new subscribers.
     */
    public ChatResponse createWelcomeNotification() {
        return new ChatResponse(SYSTEM_SENDER, MSG_WELCOME_NOTIFICATION, Instant.now(), MESSAGE_TYPE_NOTIFICATION);
    }

    /**
     * Send notification about new message to all subscribers.
     */
    private void sendNewMessageNotification(String username) {
        var notification = new ChatResponse(SYSTEM_SENDER, String.format(MSG_NEW_MESSAGE_FROM, username), Instant.now(), MESSAGE_TYPE_NOTIFICATION);

        log.info("Sending notification to {}", TOPIC_NOTIFICATIONS);
        messagingTemplate.convertAndSend(TOPIC_NOTIFICATIONS, notification);
    }

    /**
     * Broadcast system-wide notification (can be called externally).
     */
    public void broadcastSystemNotification(String message) {
        var notification = new ChatResponse(SYSTEM_SENDER, message, Instant.now(), MESSAGE_TYPE_NOTIFICATION);

        log.info("Broadcasting system notification: {}", message);
        messagingTemplate.convertAndSend(TOPIC_NOTIFICATIONS, notification);
    }
}
