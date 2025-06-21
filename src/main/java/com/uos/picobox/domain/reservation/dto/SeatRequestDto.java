package com.uos.picobox.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SeatRequestDto {

    @NotNull(message = "상영 ID는 필수입니다.")
    @Schema(description = "좌석을 선택할 상영 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long screeningId;

    @NotEmpty(message = "좌석 ID 목록은 비어있을 수 없습니다.")
    @Schema(description = "선택(또는 해제)할 좌석 ID 목록", example = "[101, 102]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> seatIds;
}