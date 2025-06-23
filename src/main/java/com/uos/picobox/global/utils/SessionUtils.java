package com.uos.picobox.global.utils;

import com.uos.picobox.admin.service.AdminDeleteService;
import com.uos.picobox.user.service.FindIdSevice;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SessionUtils {
    private final CacheManager cacheManager;
    private final FindIdSevice findIdSevice;
    private final AdminDeleteService adminDeleteService;

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
        String[] parts = Objects.requireNonNull(value).split(":", 2);
        String type = parts[0];
        String realValue = parts[1];
        LocalDateTime expirationTime = LocalDateTime.now();
        String formattedExpiration = expirationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return Map.of(
                "type", type,
                "value", realValue,
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

    public Map<String, String> splitSessionValue(String value) {
        String[] parts = Objects.requireNonNull(value).split(":", 2);
        String type = parts[0];
        String realValue = parts[1];
        return Map.of(
                "type", type,
                "value", realValue
        );
    }

    public Map<String, Object> findSessionInfoByAuthentication(Authentication authentication) {
        String value = (String) authentication.getPrincipal();
        Map<String, String> sessionInfo = splitSessionValue(value);
        String type = sessionInfo.get("type");
        Long id;
        if (type.equals("customer")) {
            String loginId = sessionInfo.get("value");
            id = findIdSevice.findCustomerIdByLoginId(loginId);
        }
        else if (type.equals("guest")) {
            String email = sessionInfo.get("value");
            id = findIdSevice.findGuestIdByEmail(email);
        }
        else {
            throw new IllegalArgumentException("잘못된 session 정보입니다.");
        }
        return Map.of(
                "type", type,
                "id", id
        );
    }

    public Long findCustomerIdByAuthentication(Authentication authentication) {
        String value = (String) authentication.getPrincipal();
        Map<String, String> sessionInfo = splitSessionValue(value);
        String type = sessionInfo.get("type");
        Long id;
        if (type.equals("customer")) {
            String loginId = sessionInfo.get("value");
            id = findIdSevice.findCustomerIdByLoginId(loginId);
        }
        else if (type.equals("guest")) {
            /*
            String email = sessionInfo.get("value");
            id = findIdSevice.findGuestIdByEmail(email);
             */
            throw new AccessDeniedException("비회원은 회원 관련 기능을 이용할 수 없습니다. 회원가입을 해주세요.");
        }
        else {
            throw new IllegalArgumentException("잘못된 session 정보입니다.");
        }
        return id;
    }

    public Long findAdminIdByAuthentication(Authentication authentication) {
        String value = (String) authentication.getPrincipal();
        Map<String, String> sessionInfo = splitSessionValue(value);
        String type = sessionInfo.get("type");
        Long id;
        if (type.equals("admin")) {
            String loginId = sessionInfo.get("value");
            id = adminDeleteService.findAdminIdByLoginId(loginId);
        }
        else if (type.equals("customer")) {
            /*
            String loginId = sessionInfo.get("value");
            id = findIdSevice.findCustomerIdByLoginId(loginId);
            */
            throw new AccessDeniedException("회원은 관리자 기능을 이용할 수 없습니다.");
        }
        else if (type.equals("guest")) {
            /*
            String email = sessionInfo.get("value");
            id = findIdSevice.findGuestIdByEmail(email);
             */
            throw new AccessDeniedException("비회원은 관리자 기능을 이용할 수 없습니다.");
        }
        else {
            throw new IllegalArgumentException("잘못된 session 정보입니다.");
        }
        return id;
    }
}
