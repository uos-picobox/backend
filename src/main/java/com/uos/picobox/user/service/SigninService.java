package com.uos.picobox.user.service;

import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.SigninRequestDto;
import com.uos.picobox.user.dto.SigninResponseDto;
import com.uos.picobox.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SigninService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionUtils sessionUtils;

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

        Map<String, String> sessionInfo = sessionUtils.createSession("customerSession", loginId);
        String sessionId = sessionInfo.get("sessionId");
        String expiration = sessionInfo.get("expiration");

        return new SigninResponseDto(loginId, sessionId, expiration);
    }
}
