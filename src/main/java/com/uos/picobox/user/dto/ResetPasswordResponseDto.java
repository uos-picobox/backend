package com.uos.picobox.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResetPasswordResponseDto {
    @Schema(description = "새 비밀번호", example = "P@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하이어야 합니다.")
    private String newPassword;
}
