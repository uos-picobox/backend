package com.uos.picobox.user.service;

import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.GuestSignoutResponseDto;
import com.uos.picobox.user.dto.SignoutResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestSignoutService {
    private final SessionUtils sessionUtils;

    public GuestSignoutResponseDto signoutGuest(String sessionId) {
        Map<String, String> evictSessionInfo = sessionUtils.evictSession("guestSession", sessionId);
        String email = evictSessionInfo.get("value");
        String expiration = evictSessionInfo.get("expiration");
        return new GuestSignoutResponseDto(email, sessionId, expiration);
    }
}
