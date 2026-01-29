package com.example.websocket.controller;

import com.example.websocket.handler.MessageBroadcastHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin REST endpoints for system operations.
 * <p>
 * NOT FOR PRODUCTION - No authentication/authorization.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MessageBroadcastHandler broadcastHandler;

    /**
     * Broadcast a system notification to all connected clients.
     * <p>
     * POST /api/admin/notify
     * Body: {"message": "Server will restart in 5 minutes"}
     */
    @PostMapping("/notify")
    public ResponseEntity<Map<String, String>> broadcastNotification(@RequestBody Map<String, String> payload) {
        var message = payload.get("message");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }

        broadcastHandler.broadcastSystemNotification(message);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Notification broadcasted"
        ));
    }
}
