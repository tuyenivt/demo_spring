package com.example.ratelimiting.ratelimit;

import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisBucketConfig {

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, byte[]> redisConnection(RedisConnectionFactory redisConnectionFactory) {
        var lettuceFactory = (LettuceConnectionFactory) redisConnectionFactory;
        var nativeClient = lettuceFactory.getNativeClient();
        if (!(nativeClient instanceof RedisClient redisClient)) {
            throw new IllegalStateException("Lettuce RedisClient is not available.");
        }
        return redisClient.connect(RedisCodec.of(new StringCodec(), new ByteArrayCodec()));
    }

    @Bean
    public LettuceBasedProxyManager<String> lettuceProxyManager(StatefulRedisConnection<String, byte[]> redisConnection) {
        return Bucket4jLettuce.casBasedBuilder(redisConnection).build();
    }
}
