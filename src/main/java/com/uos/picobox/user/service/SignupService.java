package com.uos.picobox.user.service;

import com.uos.picobox.user.dto.SignupRequestDto;
import com.uos.picobox.user.dto.SignupResponseDto;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignupService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponseDto registerCustomer(SignupRequestDto signupRequestDto) {
        if (customerRepository.existsByLoginId(signupRequestDto.getLoginId())) {
            throw new IllegalArgumentException("loginId: " + signupRequestDto.getLoginId() + "는 이미 사용 중인 로그인Id입니다.");
        }
        if (customerRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new IllegalArgumentException("email: " + signupRequestDto.getEmail() + "는 이미 사용 중인 이메일 입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        Customer customer = signupRequestDto.toEntity(encodedPassword);
        customer = customerRepository.save(customer);
        return new SignupResponseDto(customer);
    }

    public boolean isLoginIdAvailable(String loginId) {
        return !customerRepository.existsByLoginId(loginId);
    }

    public boolean isEmailAvailable(String email) {
        return !customerRepository.existsByEmail(email);
    }
}
