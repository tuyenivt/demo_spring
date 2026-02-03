package com.example.caching.service;

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

    public String getOldestUserActivity(String userId) {
        return redisTemplate.opsForList().leftPop("user:activities:" + userId);
    }
}
