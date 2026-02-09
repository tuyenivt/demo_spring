package com.example.idempotent.idempotent;

import com.example.idempotent.config.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class IdempotentRedisConfig {

    private final AppConfig appConfig;

    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper redisObjectMapper) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(appConfig.getRedisCacheTtl()))
                .disableCachingNullValues()
                .prefixCacheNameWith(appConfig.getApplicationName() + "-")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(redisObjectMapper)
                ));
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
        return template;
    }
}
