package com.example.websocket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;

/**
 * Channel interceptor that extracts username from STOMP CONNECT headers.
 * <p>
 * This is more secure than query parameters because:
 * 1. Headers are not logged by default in most servers
 * 2. Headers are not stored in browser history
 * 3. Headers don't leak via Referer
 * 4. In production, this can easily be replaced with JWT token validation
 * <p>
 * Usage: Client sends CONNECT frame with header "username: alice"
 * Server extracts and creates Principal for user-specific messaging.
 */
@Slf4j
public class UserAuthChannelInterceptor implements ChannelInterceptor {

    private static final String USERNAME_HEADER = "username";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            var username = accessor.getFirstNativeHeader(USERNAME_HEADER);

            if (username == null || username.isBlank()) {
                log.warn("No username in STOMP CONNECT headers, generating random");
                username = "user-" + System.currentTimeMillis();
            }

            log.info("STOMP CONNECT - establishing Principal for user: {}", username);

            // Create and set the Principal for this session
            var finalUsername = username;
            Principal principal = () -> finalUsername;
            accessor.setUser(principal);
        }

        return message;
    }
}
