package com.uos.picobox.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원 상태 변경 요청 DTO")
public class CustomerStatusUpdateRequestDto {
    
    @NotNull(message = "활성 상태는 필수입니다")
    @Schema(description = "회원 활성 상태 (true: 활성, false: 정지)", example = "false")
    private Boolean isActive;
    
    @Schema(description = "상태 변경 사유", example = "부적절한 행동으로 인한 정지")
    private String reason;
} 