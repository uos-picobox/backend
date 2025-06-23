package com.uos.picobox.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResetPasswordRequestDto {
    @Schema(description = "비밀번호 재설정을 위한 인증 코드", example = "1234AB", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String code;

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
}
