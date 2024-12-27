package com.example.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final StringRedisTemplate redisTemplate;

    public void addUserActivity(String userId, String activity) {
        redisTemplate.opsForList().rightPush("user:activities:" + userId, activity);
    }

    public void getOldestUserActivity(String userId) {
        redisTemplate.opsForList().leftPop("user:activities:" + userId);
    }
}
