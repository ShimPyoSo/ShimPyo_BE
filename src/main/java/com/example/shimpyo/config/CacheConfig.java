//package com.example.shimpyo.config;
//
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableCaching
//public class CacheConfig {
//
//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory cf) {
//        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
//                .disableCachingNullValues()
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
//
//        // 캐시별 TTL
//        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
//        configs.put("categoryTouristIds", base.entryTtl(Duration.ofMinutes(10))); // 카테고리 아이디 목록 10분 캐시
//
//        return RedisCacheManager.builder(cf)
//                .cacheDefaults(base)
//                .withInitialCacheConfigurations(configs)
//                .build();
//    }
//}
