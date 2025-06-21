package com.uos.picobox.user.service;

import com.uos.picobox.user.dto.SigninRequestDto;
import com.uos.picobox.user.dto.SigninResponseDto;
import com.uos.picobox.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SigninService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    public SigninResponseDto signinCustomer(SigninRequestDto signinRequestDto) {
        String loginId = signinRequestDto.getLoginId();
        String password = signinRequestDto.getPassword();

        String storedPassword = customerRepository.findPasswordByLoginId(loginId);
        if (storedPassword == null) {
            throw new IllegalArgumentException("잘못된 아이디 혹은 비밀번호입니다.");
        }

        // password is not matched
        if (!passwordEncoder.matches(password, storedPassword)) {
            throw new IllegalArgumentException("잘못된 아이디 혹은 비밀번호입니다.");
        };

        // 세션 ID 생성 (UUID 기반)
        String sessionId = java.util.UUID.randomUUID().toString();

        Cache cache = cacheManager.getCache("customerSession");
        Objects.requireNonNull(cache).put(sessionId, loginId);

        LocalDateTime expirationTime = LocalDateTime.now().plusHours(1);
        String formattedExpiration = expirationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return new SigninResponseDto(loginId, sessionId, formattedExpiration);
    }
}
