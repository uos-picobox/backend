package com.uos.picobox.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.uos.picobox.user.entity.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class SignupResponseDto {
    @Schema(description = "고객 고유 ID", example = "1")
    private Long customerId;

    @Schema(description = "로그인 아이디", example = "user123")
    private String loginId;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "성별", example = "Male")
    private String gender;

    @Schema(description = "적립 포인트", example = "1000")
    private Integer points;

    @Schema(description = "가입일", example = "2024-01-01T10:00:00")
    private LocalDateTime registeredAt;

    @Schema(description = "마지막 로그인 일시", example = "2024-04-10T14:00:00")
    private LocalDateTime lastLoginAt;

    @Schema(description = "활성 상태", example = "true")
    private Boolean isActive;

    public SignupResponseDto(Customer customer) {
        this.customerId = customer.getId();
        this.loginId = customer.getLoginId();
        this.name = customer.getName();
        this.email = customer.getEmail();
        this.phone = customer.getPhone();
        this.dateOfBirth = customer.getDateOfBirth();
        this.gender = customer.getGender();
        this.points = customer.getPoints();
        this.registeredAt = customer.getRegisteredAt();
        this.lastLoginAt = customer.getLastLoginAt();
        this.isActive = customer.getIsActive();
    }
}
