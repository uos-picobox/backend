package com.uos.picobox.user.dto;

import com.uos.picobox.user.entity.Guest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GuestSignupRequestDto {

    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email(message = "이메일 형식이어야 합니다.")
    private String email;

    @Schema(description = "생년월일", example = "1990-01-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate birthdate;

    @Schema(description = "전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Pattern(regexp = "^(010|011|016|017|018|019)-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이어야 합니다.")
    private String phone;

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

    public Guest toEntity(String encodedPassword, LocalDateTime expirationDate) {
        return Guest.builder()
                .name(this.name)
                .email(this.email)
                .birthDate(this.birthdate)
                .phone(this.phone)
                .password(encodedPassword)
                .expirationDate(expirationDate)
                .build();
    }
}
