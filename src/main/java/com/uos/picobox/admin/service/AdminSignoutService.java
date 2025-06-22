package com.uos.picobox.admin.service;

import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.SignoutResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSignoutService {
    private final SessionUtils sessionUtils;

    public SignoutResponseDto signoutAdmin(String sessionId) {
        Map<String, String> evictSessionInfo = sessionUtils.evictSession("adminSession", sessionId);
        String type = evictSessionInfo.get("type");
        if (!Objects.equals(type, "admin")) {
            throw new IllegalArgumentException("관리자 sesion이 아닙니다. 로그인을 다시 진행해주세요.");
        }
        String loginId = evictSessionInfo.get("value");
        String expiration = evictSessionInfo.get("expiration");

        return new SignoutResponseDto(loginId, sessionId, expiration);
    }
}
