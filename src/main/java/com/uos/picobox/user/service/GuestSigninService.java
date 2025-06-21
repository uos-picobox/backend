package com.uos.picobox.user.service;

import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.GuestSigninRequestDto;
import com.uos.picobox.user.dto.GuestSigninResponseDto;
import com.uos.picobox.user.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestSigninService {
    private final GuestRepository guestRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionUtils sessionUtils;

    public GuestSigninResponseDto signinGuest(GuestSigninRequestDto guestSigninRequestDto) {
        String email = guestSigninRequestDto.getEmail();
        String password = guestSigninRequestDto.getPassword();

        String storedPassword = guestRepository.findPasswordByEmail(email);
        if (storedPassword == null) {
            throw new IllegalArgumentException("잘못된 이메일 혹은 비밀번호입니다.");
        }

        // password is not matched
        if (!passwordEncoder.matches(password, storedPassword)) {
            throw new IllegalArgumentException("잘못된 이메일 혹은 비밀번호입니다.");
        };

        Map<String, String> sessionInfo = sessionUtils.createSession("guestSession", email);
        String sessionId = sessionInfo.get("sessionId");
        String expiration = sessionInfo.get("expiration");

        return new GuestSigninResponseDto(email, sessionId, expiration);
    }
}
