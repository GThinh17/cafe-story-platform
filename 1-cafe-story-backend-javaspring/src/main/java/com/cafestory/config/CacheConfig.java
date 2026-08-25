package com.cafestory.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    public static final String REGION_PROVINCES_CACHE = "regionProvinces";
    public static final String REGION_CITIES_CACHE = "regionCities";
    public static final String REGION_WARDS_CACHE = "regionWards";
    public static final String REPORT_REASONS_CACHE = "reportReasons";
    public static final String ORGANIC_FEED_CACHE = "organicFeed";
    public static final String PERSONALIZED_FEED_RANKING_CACHE = "personalizedFeedRankings";
    public static final String RECOMMENDATION_CARDS_CACHE = "recommendationCards";
    public static final String CAFE_PAGE_DETAIL_CACHE = "cafePageDetails";
    public static final String CAFE_PAGE_BLOGS_CACHE = "cafePageBlogs";
    public static final String TRENDING_BLOGS_CACHE = "trendingBlogs";
    public static final String COMMENT_LIST_CACHE = "commentLists";
    public static final String USER_FOLLOW_LIST_CACHE = "userFollowLists";
    public static final String BLOG_DETAIL_CACHE = "blogDetails";
    public static final String USER_PROFILE_BY_ID_CACHE = "userProfilesById";
    public static final String USER_PROFILE_BY_USERNAME_CACHE = "userProfilesByUsername";
    public static final String USER_PROFILE_BLOGS_CACHE = "userProfileBlogs";
    public static final String USER_FOLLOWING_COUNT_CACHE = "userFollowingCounts";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(
                redisObjectMapper(objectMapper));
        RedisCacheConfiguration defaultConfig = cacheConfiguration(jsonSerializer, Duration.ofMinutes(10));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.ofEntries(
                        Map.entry(REGION_PROVINCES_CACHE, cacheConfiguration(jsonSerializer, Duration.ofHours(24))),
                        Map.entry(REGION_CITIES_CACHE, cacheConfiguration(jsonSerializer, Duration.ofHours(24))),
                        Map.entry(REGION_WARDS_CACHE, cacheConfiguration(jsonSerializer, Duration.ofHours(24))),
                        Map.entry(REPORT_REASONS_CACHE, cacheConfiguration(jsonSerializer, Duration.ofMinutes(30))),
                        Map.entry(ORGANIC_FEED_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(PERSONALIZED_FEED_RANKING_CACHE,
                                cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(RECOMMENDATION_CARDS_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(CAFE_PAGE_DETAIL_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(CAFE_PAGE_BLOGS_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(TRENDING_BLOGS_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(COMMENT_LIST_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(10))),
                        Map.entry(USER_FOLLOW_LIST_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(BLOG_DETAIL_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(30))),
                        Map.entry(USER_PROFILE_BY_ID_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(USER_PROFILE_BY_USERNAME_CACHE,
                                cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(USER_PROFILE_BLOGS_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(15))),
                        Map.entry(USER_FOLLOWING_COUNT_CACHE, cacheConfiguration(jsonSerializer, Duration.ofSeconds(15)))))
                .transactionAware()
                .build();

        return new ResilientCacheManager(redisCacheManager);
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
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.cafestory.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .build();

        return objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(
                        typeValidator,
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY);
    }

    private RedisCacheConfiguration cacheConfiguration(
            GenericJackson2JsonRedisSerializer jsonSerializer,
            Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));
    }

    private final class ResilientCacheManager implements CacheManager {

        private final CacheManager delegate;

        private ResilientCacheManager(CacheManager delegate) {
            this.delegate = delegate;
        }

        @Override
        public Cache getCache(String name) {
            Cache cache = delegate.getCache(name);
            return cache == null ? null : new ResilientCache(cache);
        }

        @Override
        public Collection<String> getCacheNames() {
            return delegate.getCacheNames();
        }
    }

    private final class ResilientCache implements Cache {

        private final Cache delegate;

        private ResilientCache(Cache delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            try {
                return delegate.get(key);
            } catch (RuntimeException exception) {
                log.warn("Cache get failed for cache={} key={}: {}", getName(), key, exception.getMessage());
                return null;
            }
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            try {
                return delegate.get(key, type);
            } catch (RuntimeException exception) {
                log.warn("Cache get failed for cache={} key={}: {}", getName(), key, exception.getMessage());
                return null;
            }
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            try {
                return delegate.get(key, valueLoader);
            } catch (RuntimeException exception) {
                log.warn("Cache get failed for cache={} key={}: {}", getName(), key, exception.getMessage());
                return loadDirectly(key, valueLoader);
            }
        }

        @Override
        public void put(Object key, Object value) {
            try {
                delegate.put(key, value);
            } catch (RuntimeException exception) {
                log.warn("Cache put failed for cache={} key={}: {}", getName(), key, exception.getMessage());
            }
        }

        @Override
        public ValueWrapper putIfAbsent(Object key, Object value) {
            try {
                return delegate.putIfAbsent(key, value);
            } catch (RuntimeException exception) {
                log.warn("Cache putIfAbsent failed for cache={} key={}: {}", getName(), key, exception.getMessage());
                return null;
            }
        }

        @Override
        public void evict(Object key) {
            try {
                delegate.evict(key);
            } catch (RuntimeException exception) {
                log.warn("Cache evict failed for cache={} key={}: {}", getName(), key, exception.getMessage());
            }
        }

        @Override
        public boolean evictIfPresent(Object key) {
            try {
                return delegate.evictIfPresent(key);
            } catch (RuntimeException exception) {
                log.warn("Cache evict failed for cache={} key={}: {}", getName(), key, exception.getMessage());
                return false;
            }
        }

        @Override
        public void clear() {
            try {
                delegate.clear();
            } catch (RuntimeException exception) {
                log.warn("Cache clear failed for cache={}: {}", getName(), exception.getMessage());
            }
        }

        @Override
        public boolean invalidate() {
            try {
                return delegate.invalidate();
            } catch (RuntimeException exception) {
                log.warn("Cache invalidate failed for cache={}: {}", getName(), exception.getMessage());
                return false;
            }
        }

        private <T> T loadDirectly(Object key, Callable<T> valueLoader) {
            try {
                return valueLoader.call();
            } catch (Exception exception) {
                throw new ValueRetrievalException(key, valueLoader, exception);
            }
        }
    }
}
