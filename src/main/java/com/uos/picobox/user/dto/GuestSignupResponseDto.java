package com.uos.picobox.user.dto;

import com.uos.picobox.user.entity.Guest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class GuestSignupResponseDto {
    @Schema(description = "비회원 고유 ID", example = "1")
    private Long id;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthDate;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "만료시간", example = "2024-04-10T14:00:00")
    private LocalDateTime expirationDate;

    @Schema(description = "login session info", example = "a3dxg-923a12-xyz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionId;

    @Schema(description = "로그인 만료 시간", example = "2024-01-01 10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expiresAt;

    public GuestSignupResponseDto(Guest guest, String sessionId, String expiresAt) {
        this.id = guest.getId();
        this.name = guest.getName();
        this.email = guest.getEmail();
        this.birthDate = guest.getBirthDate();
        this.phone = guest.getPhone();
        this.expirationDate = guest.getExpirationDate();
        this.sessionId = sessionId;
        this.expiresAt = expiresAt;
    }
}
