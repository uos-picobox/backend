package com.uos.picobox.admin.service;

import com.uos.picobox.admin.dto.AdminSignupRequestDto;
import com.uos.picobox.admin.dto.AdminSignupResponseDto;
import com.uos.picobox.admin.entity.Admin;
import com.uos.picobox.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSignupService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminSignupResponseDto registerAdmin(AdminSignupRequestDto adminSignupRequestDto) {
        if (adminRepository.existsByLoginId(adminSignupRequestDto.getLoginId())) {
            throw new IllegalArgumentException("loginId: " + adminSignupRequestDto.getLoginId() + "는 이미 사용 중인 로그인Id입니다.");
        }
        if (adminRepository.existsByEmail(adminSignupRequestDto.getEmail())) {
            throw new IllegalArgumentException("email: " + adminSignupRequestDto.getEmail() + "는 이미 사용 중인 이메일 입니다.");
        }

        if (!adminSignupRequestDto.getAdminCode().equals("ABCD1234")) {
            throw new IllegalArgumentException("관리자 인증 키: " + adminSignupRequestDto.getAdminCode() + "는 올바르지 않은 인증 키 입니다. 사내 부서에 문의하세요.");
        }

        String encodedPassword = passwordEncoder.encode(adminSignupRequestDto.getPassword());

        Admin admin = adminSignupRequestDto.toEntity(encodedPassword);
        admin = adminRepository.save(admin);
        return new AdminSignupResponseDto(admin);
    }

    public boolean isLoginIdAvailable(String loginId) {
        return !adminRepository.existsByLoginId(loginId);
    }

    public boolean isEmailAvailable(String email) {
        return !adminRepository.existsByEmail(email);
    }
}
