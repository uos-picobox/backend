package com.uos.picobox.global.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SessionUtils {
    private final CacheManager cacheManager;

    public Map<String, String> createSession(String cacheName, String value) {
        String sessionId = java.util.UUID.randomUUID().toString();
        Cache cache = cacheManager.getCache(cacheName);
        Objects.requireNonNull(cache).put(sessionId, value);

        String expiration = LocalDateTime.now()
                .plusHours(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return Map.of(
                "sessionId", sessionId,
                "expiration", expiration
        );
    }

    public Map<String, String> evictSession(String cacheName, String sessionId) {
        Cache cache = cacheManager.getCache(cacheName);
        Cache.ValueWrapper wrapper = Objects.requireNonNull(cache).get(sessionId);
        if (Objects.isNull(wrapper)) {
            throw new IllegalArgumentException("잘못된 session이거나 이미 만료된 session입니다.");
        }
        cache.evict(sessionId);

        String value = (String) wrapper.get();
        LocalDateTime expirationTime = LocalDateTime.now();
        String formattedExpiration = expirationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return Map.of(
                "value", Objects.requireNonNull(value),
                "expiration", formattedExpiration
        );
    }

    public String existSession(String cacheName, String sessionId) {
        Cache cache = cacheManager.getCache(cacheName);
        Cache.ValueWrapper wrapper = Objects.requireNonNull(cache).get(sessionId);
        if (Objects.isNull(wrapper)) {
            throw new IllegalArgumentException("잘못된 session이거나 이미 만료된 session입니다.");
        }
        String value = (String) wrapper.get();
        return Objects.requireNonNull(value);
    }
}
