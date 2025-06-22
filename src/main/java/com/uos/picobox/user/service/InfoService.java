package com.uos.picobox.user.service;

import com.uos.picobox.user.dto.CustomerInfoRequestDto;
import com.uos.picobox.user.dto.CustomerInfoResponseDto;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InfoService {
    private final CustomerRepository customerRepository;
    private final SignupService signupService;

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
            if (!signupService.isLoginIdAvailable(dto.getLoginId())) {
                throw new IllegalArgumentException("중복된 loginId 입니다.");
            }
            customer.setLoginId(dto.getLoginId());
        }
        if (dto.getName() != null) {
            customer.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            if (!signupService.isEmailAvailable(dto.getEmail())) {
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
}
