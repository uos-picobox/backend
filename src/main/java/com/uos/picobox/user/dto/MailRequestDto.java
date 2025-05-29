package com.uos.picobox.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MailRequestDto {
    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;

    @Schema(description = "이메일 전송 목적 (예: 회원가입, 비밀번호 재설정)", example = "회원가입")
    @NotBlank(message = "필수 항목입니다.")
    private String purpose;
}
