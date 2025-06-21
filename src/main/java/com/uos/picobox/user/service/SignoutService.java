package com.uos.picobox.user.service;

import com.uos.picobox.user.dto.SignoutResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignoutService {
    private final CacheManager cacheManager;

    public SignoutResponseDto signoutCustomer(String sessionId) {
        Cache cache = cacheManager.getCache("customerSession");
        Cache.ValueWrapper wrapper = Objects.requireNonNull(cache).get(sessionId);
        if (Objects.isNull(wrapper)) {
            throw new IllegalArgumentException("잘못된 session이거나 이미 만료된 session입니다.");
        }
        cache.evict(sessionId);
        String loginId = (String) wrapper.get();

        LocalDateTime expirationTime = LocalDateTime.now();
        String formattedExpiration = expirationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return new SignoutResponseDto(loginId, sessionId, formattedExpiration);
    }

}
