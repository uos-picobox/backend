package com.uos.picobox.user.service;

import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.SigninRequestDto;
import com.uos.picobox.user.dto.SigninResponseDto;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SigninService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionUtils sessionUtils;

    @Transactional
    public SigninResponseDto signinCustomer(SigninRequestDto signinRequestDto) {
        String loginId = signinRequestDto.getLoginId();
        String password = signinRequestDto.getPassword();

        Customer customer = customerRepository.findByLoginId(loginId).orElseThrow(() ->
                new EntityNotFoundException("잘못된 아이디 혹은 비밀번호입니다."));
        if (!customer.getIsActive()) {
            throw new AccessDeniedException("활동 정지 상태 계정입니다. 관리자에게 문의해주세요.");
        }

        String storedPassword = customer.getPassword();
        if (storedPassword == null) {
            throw new IllegalArgumentException("잘못된 아이디 혹은 비밀번호입니다.");
        }

        // password is not matched
        if (!passwordEncoder.matches(password, storedPassword)) {
            throw new IllegalArgumentException("잘못된 아이디 혹은 비밀번호입니다.");
        }

        // Last Login Time 업데이트.
        customer.updateLastLoginAt();

        Map<String, String> sessionInfo = sessionUtils.createSession("userSession", "customer:" + loginId);
        String sessionId = sessionInfo.get("sessionId");
        String expiration = sessionInfo.get("expiration");

        return new SigninResponseDto(loginId, sessionId, expiration);
    }
}
