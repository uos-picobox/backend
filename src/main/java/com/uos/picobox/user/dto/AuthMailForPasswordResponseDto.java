package com.uos.picobox.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthMailForPasswordResponseDto {
    @Schema(description = "비밀번호 재설정을 위한 인증 코드", example = "1234AB", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
