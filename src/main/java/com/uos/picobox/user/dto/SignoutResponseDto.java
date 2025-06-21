package com.uos.picobox.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignoutResponseDto {
    @Schema(description = "로그인 아이디", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String loginId;

    @Schema(description = "login session info", example = "a3dxg-923a12-xyz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionId;

    @Schema(description = "로그인 아웃 시간", example = "2024-01-01 10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expiresAt;
}
