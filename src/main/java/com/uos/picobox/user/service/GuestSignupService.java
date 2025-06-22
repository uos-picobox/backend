package com.uos.picobox.user.service;

import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.GuestSignupRequestDto;
import com.uos.picobox.user.dto.GuestSignupResponseDto;
import com.uos.picobox.user.entity.Guest;
import com.uos.picobox.user.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestSignupService {

    private final GuestRepository guestRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionUtils sessionUtils;

    @Transactional
    public GuestSignupResponseDto registerGuest(GuestSignupRequestDto guestSignupRequestDto) {
        if (guestRepository.existsByEmail(guestSignupRequestDto.getEmail())) {
            throw new IllegalArgumentException("email: " + guestSignupRequestDto.getEmail() + "는 이미 사용 중인 이메일 입니다.");
        }

        String encodedPassword = passwordEncoder.encode(guestSignupRequestDto.getPassword());
        // 6시간 후 만료로 설정 후 예매 시 상영 종료 시점까지 연장.
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(6);

        Guest guest = guestSignupRequestDto.toEntity(encodedPassword, expirationDate);
        guest = guestRepository.save(guest);

        Map<String, String> sessionInfo = sessionUtils.createSession("userSession", "guest:" + guest.getEmail());
        String sessionId = sessionInfo.get("sessionId");
        String expiration = sessionInfo.get("expiration");

        return new GuestSignupResponseDto(guest, sessionId, expiration);
    }


    public boolean isEmailAvailable(String email) {
        return !guestRepository.existsByEmail(email);
    }
}
