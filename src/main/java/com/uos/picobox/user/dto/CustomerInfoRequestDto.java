package com.uos.picobox.user.dto;

import com.uos.picobox.global.enumClass.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class CustomerInfoRequestDto {
    @Schema(description = "고객 고유 ID", example = "1")
    @NotNull
    private Long customerId;

    @Schema(description = "로그인 아이디", example = "user123")
    private String loginId;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "성별", example = "Male")
    private Gender gender;
}
