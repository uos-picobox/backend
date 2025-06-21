package com.uos.picobox.admin.dto;

import com.uos.picobox.admin.entity.Admin;
import com.uos.picobox.global.enumClass.AdminRole;
import com.uos.picobox.user.entity.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class AdminSignupResponseDto {
    @Schema(description = "고객 고유 ID", example = "1")
    private Long adminId;

    @Schema(description = "로그인 아이디", example = "user123")
    private String loginId;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "가입일", example = "2024-01-01T10:00:00")
    private LocalDateTime registeredAt;

    @Schema(description = "마지막 로그인 일시", example = "2024-04-10T14:00:00")
    private LocalDateTime lastLoginAt;

    @Schema(
            description = "관리자 권한 등급 (SUPER: 전체 권한, USER_MANAGE: 유저 관리, MOVIE_MANAGE: 영화 관리, SCREEN_MANAGE: 상영 관리)",
            example = "SUPER",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private AdminRole role;

    @Schema(description = "활성 상태", example = "true")
    private Boolean isActive;

    public AdminSignupResponseDto(Admin admin) {
        this.adminId = admin.getId();
        this.loginId = admin.getLoginId();
        this.name = admin.getName();
        this.email = admin.getEmail();
        this.registeredAt = admin.getRegisteredAt();
        this.lastLoginAt = admin.getLastLoginAt();
        this.isActive = admin.getIsActive();
        this.role = admin.getRole();
    }
}
