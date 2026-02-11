package com.example.websocket.config;

import com.example.websocket.handler.MessageBroadcastHandler;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.example.websocket.constant.WebSocketDestinations.MSG_SERVER_SHUTTING_DOWN;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketShutdownNotifier {

    private final MessageBroadcastHandler broadcastHandler;

    @PreDestroy
    public void notifyClients() {
        log.info("Broadcasting shutdown notification to connected clients");
        broadcastHandler.broadcastSystemNotification(MSG_SERVER_SHUTTING_DOWN);
    }
}
