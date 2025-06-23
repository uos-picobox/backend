package com.uos.picobox.admin.service;

import com.uos.picobox.admin.dto.CustomerManagementResponseDto;
import com.uos.picobox.admin.dto.CustomerStatusUpdateRequestDto;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCustomerManagementService {

    private final CustomerRepository customerRepository;

    /**
     * 전체 회원 목록 조회
     */
    public List<CustomerManagementResponseDto> getAllCustomers(String sort, Boolean isActive) {
        log.info("관리자 전체 회원 조회: sort={}, isActive={}", sort, isActive);
        
        List<Customer> customers;
        if (isActive != null) {
            customers = customerRepository.findByIsActive(isActive);
        } else {
            customers = customerRepository.findAll();
        }
        
        // 정렬 적용
        customers = sortCustomers(customers, sort);
        
        return customers.stream()
                .map(CustomerManagementResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 회원 상세 조회
     */
    public CustomerManagementResponseDto getCustomerDetail(Long customerId) {
        log.info("관리자 회원 상세 조회: customerId={}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다: " + customerId));
        
        return CustomerManagementResponseDto.fromEntity(customer);
    }

    /**
     * 회원 상태 변경 (활성/정지)
     */
    @Transactional
    public void updateCustomerStatus(Long customerId, CustomerStatusUpdateRequestDto request) {
        log.info("관리자 회원 상태 변경: customerId={}, isActive={}, reason={}", 
                customerId, request.getIsActive(), request.getReason());
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다: " + customerId));
        
        customer.setIsActive(request.getIsActive());
        customerRepository.save(customer);
        
        log.info("회원 상태 변경 완료: customerId={}, 새로운 상태={}", customerId, request.getIsActive());
    }

    /**
     * 로그인 ID로 회원 검색
     */
    public List<CustomerManagementResponseDto> searchCustomersByLoginId(String loginId) {
        log.info("관리자 회원 로그인ID 검색: loginId={}", loginId);
        
        List<Customer> customers = customerRepository.findByLoginIdContainingIgnoreCase(loginId);
        
        return customers.stream()
                .map(CustomerManagementResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 이름으로 회원 검색
     */
    public List<CustomerManagementResponseDto> searchCustomersByName(String name) {
        log.info("관리자 회원 이름 검색: name={}", name);
        
        List<Customer> customers = customerRepository.findByNameContainingIgnoreCase(name);
        
        return customers.stream()
                .map(CustomerManagementResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 이메일로 회원 검색
     */
    public List<CustomerManagementResponseDto> searchCustomersByEmail(String email) {
        log.info("관리자 회원 이메일 검색: email={}", email);
        
        List<Customer> customers = customerRepository.findByEmailContainingIgnoreCase(email);
        
        return customers.stream()
                .map(CustomerManagementResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 고객 목록 정렬
     */
    private List<Customer> sortCustomers(List<Customer> customers, String sort) {
        return switch (sort) {
            case "name" -> customers.stream()
                    .sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
                    .collect(Collectors.toList());
            case "registeredAt" -> customers.stream()
                    .sorted((c1, c2) -> c2.getRegisteredAt().compareTo(c1.getRegisteredAt()))
                    .collect(Collectors.toList());
            case "lastLoginAt" -> customers.stream()
                    .sorted((c1, c2) -> {
                        if (c1.getLastLoginAt() == null && c2.getLastLoginAt() == null) return 0;
                        if (c1.getLastLoginAt() == null) return 1;
                        if (c2.getLastLoginAt() == null) return -1;
                        return c2.getLastLoginAt().compareTo(c1.getLastLoginAt());
                    })
                    .collect(Collectors.toList());
            case "points" -> customers.stream()
                    .sorted((c1, c2) -> c2.getPoints().compareTo(c1.getPoints()))
                    .collect(Collectors.toList());
            default -> customers.stream()
                    .sorted((c1, c2) -> c2.getRegisteredAt().compareTo(c1.getRegisteredAt()))
                    .collect(Collectors.toList());
        };
    }
} 