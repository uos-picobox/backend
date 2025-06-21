package com.uos.picobox.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GuestSigninResponseDto {

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "login session info", example = "a3dxg-923a12-xyz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionId;

    @Schema(description = "로그인 만료 시간", example = "2024-01-01 10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expiresAt;
}
