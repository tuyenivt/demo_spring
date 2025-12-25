package com.example.ratelimiting.ratelimit;

import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisBucketConfig {

    @Bean
    public LettuceBasedProxyManager<String> lettuceProxyManager(StatefulRedisConnection<String, byte[]> redisConnection) {
        return Bucket4jLettuce.casBasedBuilder(redisConnection).build();
    }
}
