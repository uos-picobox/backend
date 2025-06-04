package com.uos.picobox.domain.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketTypeRequestDto {

    @NotBlank(message = "티켓 종류 이름은 필수입니다.")
    @Size(max = 30, message = "티켓 종류 이름은 최대 30자까지 입력 가능합니다.")
    @Schema(description = "티켓 종류 이름 (예: 성인, 청소년)", example = "성인", requiredMode = Schema.RequiredMode.REQUIRED)
    private String typeName;

    @Size(max = 500, message = "설명은 최대 500자까지 입력 가능합니다.")
    @Schema(description = "티켓 종류에 대한 설명 (선택 사항)", example = "만 19세 이상 일반 관람객")
    private String description;
}