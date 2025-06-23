package com.uos.picobox.user.service;

import com.uos.picobox.global.utils.PasswordUtils;
import com.uos.picobox.user.dto.*;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerInfoService {
    private final CustomerRepository customerRepository;
    private final SignupService signupService;
    private final PasswordUtils passwordUtils;
    private final PasswordEncoder passwordEncoder;

    public CustomerInfoResponseDto findCustomerInfoById(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() ->
                new EntityNotFoundException("해당 회원 정보가 존재하지 않습니다."));
        return new CustomerInfoResponseDto(customer);
    }

    @Transactional
    public CustomerInfoResponseDto updateCustomerInfo(CustomerInfoRequestDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("해당 회원 정보가 존재하지 않습니다."));

        if (dto.getLoginId() != null) {
            if (!dto.getLoginId().equals(customer.getLoginId()) && !signupService.isLoginIdAvailable(dto.getLoginId())) {
                throw new IllegalArgumentException("중복된 loginId 입니다.");
            }
            customer.setLoginId(dto.getLoginId());
        }
        if (dto.getName() != null) {
            customer.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            if (!dto.getEmail().equals(customer.getEmail()) && !signupService.isEmailAvailable(dto.getEmail())) {
                throw new IllegalArgumentException("중복된 email 입니다.");
            }
            customer.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            customer.setPhone(dto.getPhone());
        }
        if (dto.getDateOfBirth() != null) {
            customer.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getGender() != null) {
            customer.setGender(dto.getGender());
        }

        return new CustomerInfoResponseDto(customer);
    }

    public boolean existsEmailAndName(String email, String name) {
        return customerRepository.existsByEmailAndName(email, name);
    }

    public boolean existsLoginIdAndEmail(String loginId, String email) {
        return customerRepository.existsByLoginIdAndEmail(loginId, email);
    }

    public AuthMailForLoginIdResponseDto findLoginIdByEmail(String email) {
        if (signupService.isEmailAvailable(email)) {
            throw new IllegalArgumentException("존재하지 않는 이메일 정보입니다.");
        }
        String loginId = customerRepository.findLoginIdByEmail(email);
        return new AuthMailForLoginIdResponseDto(loginId);
    }

    @Transactional
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto dto) {
        if (!dto.getPassword().equals(dto.getRepeatPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        String email = passwordUtils.findEmailByAuthCode(dto.getCode());
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        customerRepository.updatePasswordByEmail(email, encodedPassword);
        return new ResetPasswordResponseDto(dto.getPassword());
    }

    @Transactional
    public void deleteCustomerById(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("존재하지 않는 회원 ID입니다.");
        }
        customerRepository.deleteById(customerId);
    }
}
