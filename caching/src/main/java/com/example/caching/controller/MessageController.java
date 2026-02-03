package com.example.caching.controller;

import com.example.caching.sender.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final RedisPublisher redisPublisher;

    @PostMapping("/publish")
    public ResponseEntity<String> publish(@RequestParam(defaultValue = "my-channel") String channel, @RequestBody String message) {
        redisPublisher.publish(channel, message);
        return ResponseEntity.ok("Message published to " + channel);
    }
}
