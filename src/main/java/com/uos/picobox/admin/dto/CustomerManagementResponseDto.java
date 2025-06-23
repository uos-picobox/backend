package com.uos.picobox.admin.dto;

import com.uos.picobox.global.enumClass.Gender;
import com.uos.picobox.user.entity.Customer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerManagementResponseDto {
    private Long customerId;
    private String loginId;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Integer points;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;
    private Boolean isActive;

    public static CustomerManagementResponseDto fromEntity(Customer customer) {
        return CustomerManagementResponseDto.builder()
                .customerId(customer.getId())
                .loginId(customer.getLoginId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .points(customer.getPoints())
                .registeredAt(customer.getRegisteredAt())
                .lastLoginAt(customer.getLastLoginAt())
                .isActive(customer.getIsActive())
                .build();
    }
} 