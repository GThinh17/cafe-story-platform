package com.cafestory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    public static final String REGION_PROVINCES_CACHE = "regionProvinces";
    public static final String REGION_CITIES_CACHE = "regionCities";
    public static final String REGION_WARDS_CACHE = "regionWards";
    public static final String REPORT_REASONS_CACHE = "reportReasons";
    public static final String ORGANIC_FEED_CACHE = "organicFeed";
    public static final String BLOG_DETAIL_CACHE = "blogDetails";
    public static final String USER_PROFILE_BY_ID_CACHE = "userProfilesById";
    public static final String USER_PROFILE_BY_USERNAME_CACHE = "userProfilesByUsername";
    public static final String USER_PROFILE_BLOGS_CACHE = "userProfileBlogs";
    public static final String USER_FOLLOWING_COUNT_CACHE = "userFollowingCounts";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper(objectMapper));
        RedisCacheConfiguration defaultConfig = cacheConfiguration(jsonSerializer, Duration.ofMinutes(10));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.of(
                        REGION_PROVINCES_CACHE, cacheConfiguration(jsonSerializer, Duration.ofHours(24)),
                        REGION_CITIES_CACHE, cacheConfiguration(jsonSerializer, Duration.ofHours(24)),
                        REGION_WARDS_CACHE, cacheConfiguration(jsonSerializer, Duration.ofHours(24)),
                        REPORT_REASONS_CACHE, cacheConfiguration(jsonSerializer, Duration.ofMinutes(30)),
                        ORGANIC_FEED_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(45)),
                        BLOG_DETAIL_CACHE, cacheConfiguration(jsonSerializer, Duration.ofMinutes(5)),
                        USER_PROFILE_BY_ID_CACHE, cacheConfiguration(jsonSerializer, Duration.ofMinutes(5)),
                        USER_PROFILE_BY_USERNAME_CACHE, cacheConfiguration(jsonSerializer, Duration.ofMinutes(5)),
                        USER_PROFILE_BLOGS_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(90)),
                        USER_FOLLOWING_COUNT_CACHE, cacheConfiguration(jsonSerializer, Duration.ofMinutes(5))))
                .transactionAware()
                .build();
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get failed for cache={} key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put failed for cache={} key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict failed for cache={} key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear failed for cache={}", cache.getName(), exception);
            }
        };
    }

    private ObjectMapper redisObjectMapper(ObjectMapper objectMapper) {
        return objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private RedisCacheConfiguration cacheConfiguration(
            GenericJackson2JsonRedisSerializer jsonSerializer,
            Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));
    }
}
