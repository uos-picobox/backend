package com.uos.picobox.domain.screening.dto;

import com.uos.picobox.domain.screening.entity.ScreeningSeat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScreeningSeatStatusDto {
    @Schema(description = "좌석 고유 ID", example = "101")
    private Long seatId;

    @Schema(description = "좌석 번호", example = "A1")
    private String seatNumber;

    @Schema(description = "좌석 상태", example = "AVAILABLE")
    private String status;

    public static ScreeningSeatStatusDto fromEntity(ScreeningSeat screeningSeat) {
        return ScreeningSeatStatusDto.builder()
                .seatId(screeningSeat.getSeat().getId())
                .seatNumber(screeningSeat.getSeat().getSeatNumber())
                .status(screeningSeat.getSeatStatus().name())
                .build();
    }
}