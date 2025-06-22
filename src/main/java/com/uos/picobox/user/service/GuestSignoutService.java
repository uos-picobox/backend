package com.uos.picobox.user.service;

import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.GuestSignoutResponseDto;
import com.uos.picobox.user.dto.SignoutResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestSignoutService {
    private final SessionUtils sessionUtils;

    public GuestSignoutResponseDto signoutGuest(String sessionId) {
        Map<String, String> evictSessionInfo = sessionUtils.evictSession("userSession", sessionId);
        String type = evictSessionInfo.get("type");
        if (!Objects.equals(type, "guest")) {
            throw new IllegalArgumentException("비회원 sesion이 아닙니다. 로그인을 다시 진행해주세요.");
        }
        String email = evictSessionInfo.get("value");
        String expiration = evictSessionInfo.get("expiration");
        return new GuestSignoutResponseDto(email, sessionId, expiration);
    }
}
