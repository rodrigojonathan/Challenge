package com.challenge.config;

import com.challenge.dto.PostResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    public static final String POSTS_ALL_CACHE = "posts:all";

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory cf, ObjectMapper mapper) {
        var keySer = new StringRedisSerializer();

        var om = mapper.copy();

        var listType = om.getTypeFactory()
                .constructCollectionType(java.util.List.class, PostResponseDTO.class);

        var listSer = new Jackson2JsonRedisSerializer<>(om, listType);
        var defaultSer = new GenericJackson2JsonRedisSerializer(om);

        var defaultCfg = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(defaultSer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10));

        var postsCfg = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(listSer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaultCfg)
                .withInitialCacheConfigurations(Map.of(POSTS_ALL_CACHE, postsCfg))
                .build();
    }

}
