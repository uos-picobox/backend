package com.uos.picobox.admin.service;

import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.SignoutResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSignoutService {
    private final SessionUtils sessionUtils;

    public SignoutResponseDto signoutAdmin(String sessionId) {
        Map<String, String> evictSessionInfo = sessionUtils.evictSession("adminSession", sessionId);
        String loginId = evictSessionInfo.get("value");
        String expiration = evictSessionInfo.get("expiration");

        return new SignoutResponseDto(loginId, sessionId, expiration);
    }
}
