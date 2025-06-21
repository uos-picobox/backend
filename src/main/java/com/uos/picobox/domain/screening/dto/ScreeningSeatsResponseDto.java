package com.uos.picobox.domain.screening.dto;

import com.uos.picobox.domain.screening.entity.Screening;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ScreeningSeatsResponseDto {

    @Schema(description = "상영 정보")
    private ScreeningResponseDto screeningInfo;

    @Schema(description = "좌석 목록")
    private List<ScreeningSeatStatusDto> seats;

    public static ScreeningSeatsResponseDto toDto(Screening screening, List<ScreeningSeatStatusDto> seats) {
        return ScreeningSeatsResponseDto.builder()
                .screeningInfo(new ScreeningResponseDto(screening))
                .seats(seats)
                .build();
    }
}