package com.uos.picobox.user.dto;

import com.uos.picobox.user.entity.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequestDto {
    @Schema(description = "로그인 아이디", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email(message = "이메일 형식이어야 합니다.")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Pattern(regexp = "^(010|011|016|017|018|019)-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이어야 합니다.")
    private String phone;

    @Schema(description = "생년월일", example = "1990-01-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate dateOfBirth;

    @Schema(description = "성별", example = "Male", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String gender;

    public Customer toEntity(String encodedPassword) {
        return Customer.builder()
                .loginId(this.loginId)
                .password(encodedPassword)
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
                .dateOfBirth(this.dateOfBirth)
                .gender(this.gender)
                .build();
    }
}
