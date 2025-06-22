package com.uos.picobox.global.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        Map<String, Caffeine<Object, Object>> caffeineConfigs = new HashMap<>();
        caffeineConfigs.put("emailAuthCode", Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES));
        caffeineConfigs.put("userSession", Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS));
        caffeineConfigs.put("adminSession", Caffeine.newBuilder().expireAfterWrite(6, TimeUnit.HOURS));

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> caches = caffeineConfigs.entrySet().stream()
                .map(entry -> new CaffeineCache(entry.getKey(), entry.getValue().build()))
                .collect(Collectors.toList());

        cacheManager.setCaches(caches);
        return cacheManager;
    }
}