package com.uos.picobox.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MyPointsResponseDto {
    @Schema(description = "고객 고유 ID", example = "1")
    private Long customerId;
    @Schema(description = "로그인 아이디", example = "user123")
    private String loginId;
    @Schema(description = "적립 포인트", example = "1000")
    private Integer points;
}
