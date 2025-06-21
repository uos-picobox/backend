package com.uos.picobox.user.service;

import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.SignoutResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignoutService {
    private final SessionUtils sessionUtils;

    public SignoutResponseDto signoutCustomer(String sessionId) {
        Map<String, String> evictSessionInfo = sessionUtils.evictSession("customerSession", sessionId);
        String loginId = evictSessionInfo.get("value");
        String expiration = evictSessionInfo.get("expiration");
        return new SignoutResponseDto(loginId, sessionId, expiration);
    }

}
