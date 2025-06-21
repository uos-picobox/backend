package com.uos.picobox.admin.dto;

import com.uos.picobox.admin.entity.Admin;
import com.uos.picobox.global.enumClass.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminSignupRequestDto {
    @Schema(description = "로그인 아이디", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 12, message = "1~12자 사이여야 합니다.")
    private String loginId;

    /*@Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\\$%^&*()_+\\-={}:;\"'<>?,./]).+$",
            message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    ) */
    @Schema(description = "비밀번호", example = "P@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하이어야 합니다.")
    private String password;

    @Schema(description = "비밀번호 확인", example = "P@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String repeatPassword;

    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "이메일 주소", example = "admin@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email(message = "이메일 형식이어야 합니다.")
    private String email;

    @Schema(
            description = "관리자 권한 등급 (SUPER: 전체 권한, USER_MANAGE: 유저 관리, MOVIE_MANAGE: 영화 관리, SCREEN_MANAGE: 상영 관리)",
            example = "SUPER",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private AdminRole role;

    @Schema(
        description = "관리자 등록 코드 (시스템에서 발급된 랜덤 문자열), 실제 코드도 이것과 같아요.",
        example = "ABCD1234",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String adminCode;

    public Admin toEntity(String encodedPassword) {
        return Admin.builder()
                .loginId(this.loginId)
                .password(encodedPassword)
                .name(this.name)
                .email(this.email)
                .role(this.role)
                .build();
    }
}
