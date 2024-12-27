package com.example.redis.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class RedisSubscriber implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        var channel = new String(pattern, StandardCharsets.UTF_8);
        var msg = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Received message: {} from channel: {}", msg, channel);
        // Add your business logic here
    }
}
