package com.uos.picobox.global.utils;

import com.uos.picobox.user.dto.AuthMailForPasswordResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PasswordUtils {
    private final CacheManager cacheManager;
    private final EmailUtils emailUtils;

    public AuthMailForPasswordResponseDto setAuthCode(String email) {
        Cache cache = cacheManager.getCache("resetPassword");
        String authCode = emailUtils.createAuthCode();
        Objects.requireNonNull(cache).put(authCode, email);
        return new AuthMailForPasswordResponseDto(authCode);
    }

    public String findEmailByAuthCode(String authCode) {
        Cache cache = cacheManager.getCache("resetPassword");
        Cache.ValueWrapper wrapper = Objects.requireNonNull(cache).get(authCode);
        if (Objects.isNull(wrapper)) {
            throw new IllegalArgumentException("잘못된 인증 코드이거나 이미 만료된 코드입니다.");
        }
        Object raw = wrapper.get();
        if (raw instanceof String) {
            Objects.requireNonNull(cache).evict(authCode);
            return (String) raw;
        }
        else {
            throw new IllegalArgumentException("이메일 정보를 찾을 수 없습니다.");
        }
    }
}
