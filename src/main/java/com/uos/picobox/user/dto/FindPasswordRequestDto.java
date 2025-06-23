package com.uos.picobox.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindPasswordRequestDto {
    @Schema(description = "로그인 아이디", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 12, message = "1~12자 사이여야 합니다.")
    private String loginId;

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;
}
