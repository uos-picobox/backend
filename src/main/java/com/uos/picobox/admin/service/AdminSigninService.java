package com.uos.picobox.admin.service;

import com.uos.picobox.admin.entity.Admin;
import com.uos.picobox.admin.repository.AdminRepository;
import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.SigninRequestDto;
import com.uos.picobox.user.dto.SigninResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSigninService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionUtils sessionUtils;

    @Transactional
    public SigninResponseDto signinAdmin(SigninRequestDto signinRequestDto) {
        String loginId = signinRequestDto.getLoginId();
        String password = signinRequestDto.getPassword();

        Admin admin = adminRepository.findByLoginId(loginId).orElseThrow(() ->
                new EntityNotFoundException("잘못된 아이디 혹은 비밀번호입니다."));
        if (!admin.getIsActive()) {
            throw new AccessDeniedException("활동 정지 계정입니다. 사내에 문의해주세요");
        }

        String storedPassword = admin.getPassword();
        if (storedPassword == null) {
            throw new IllegalArgumentException("잘못된 아이디 혹은 비밀번호입니다.");
        }

        // password is not matched
        if (!passwordEncoder.matches(password, storedPassword)) {
            throw new IllegalArgumentException("잘못된 아이디 혹은 비밀번호입니다.");
        }

        // Last Login Time 업데이트.
        admin.updateLastLoginAt();

        Map<String, String> sessionInfo = sessionUtils.createSession("adminSession", "admin:" + loginId);
        String sessionId = sessionInfo.get("sessionId");
        String expiration = sessionInfo.get("expiration");
        return new SigninResponseDto(loginId, sessionId, expiration);
    }
}
