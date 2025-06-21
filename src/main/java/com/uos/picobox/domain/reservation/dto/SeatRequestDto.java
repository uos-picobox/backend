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
    @Schema(description = "상영 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long screeningId;

    @NotEmpty(message = "좌석 ID 목록은 최소 하나 이상 있어야 합니다.")
    @Schema(description = "좌석 ID 목록", example = "[101, 102, 103]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> seatIds;

    public SeatRequestDto(Long screeningId, List<Long> seatIds) {
        this.screeningId = screeningId;
        this.seatIds = seatIds;
    }
}