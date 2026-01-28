package com.example.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static com.example.websocket.constant.WebSocketDestinations.*;

/**
 * WebSocket configuration for STOMP messaging.
 * <p>
 * This configuration:
 * 1. Enables STOMP over WebSocket with SockJS fallback
 * 2. Configures an in-memory message broker for pub/sub
 * 3. Sets up application destination prefix for message routing
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker options.
     * <p>
     * - Simple broker: In-memory broker that handles subscriptions and broadcasting
     * - /topic: For broadcast messages (one-to-many)
     * - /queue: For point-to-point messages (one-to-one)
     * - /app: Prefix for messages bound for @MessageMapping methods
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker
        // Messages sent to /topic or /queue will be broadcast to subscribers
        config.enableSimpleBroker(TOPIC_DESTINATION_PREFIX, QUEUE_DESTINATION_PREFIX);

        // Prefix for application destination mappings
        // Client sends to /app/chat.send → routes to @MessageMapping("/chat.send")
        config.setApplicationDestinationPrefixes(APP_DESTINATION_PREFIX);

        // Optional: Set prefix for user-specific destinations
        // Allows sending to /user/{username}/queue/reply
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints with SockJS fallback.
     * <p>
     * SockJS provides WebSocket emulation for browsers that don't support WebSocket.
     * It tries WebSocket first, then falls back to HTTP-based transports.
     * <p>
     * Fallback chain: WebSocket → xhr-streaming → xhr-polling → etc.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WEBSOCKET_ENDPOINT)
                .setAllowedOriginPatterns("*") // In production, specify exact origins
                .withSockJS(); // Enable SockJS fallback
    }
}
