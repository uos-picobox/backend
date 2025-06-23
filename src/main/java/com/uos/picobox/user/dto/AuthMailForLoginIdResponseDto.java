package com.uos.picobox.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthMailForLoginIdResponseDto {
    @Schema(description = "로그인 아이디", example = "user123")
    private String loginId;
}
