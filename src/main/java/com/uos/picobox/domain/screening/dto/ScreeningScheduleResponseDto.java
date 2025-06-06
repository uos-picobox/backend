package com.uos.picobox.domain.screening.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uos.picobox.domain.screening.entity.Screening;
import com.uos.picobox.domain.screening.entity.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScreeningScheduleResponseDto {

    @Schema(description = "상영 ID", example = "1")
    private Long screeningId;

    @Schema(description = "영화 제목", example = "범죄도시4")
    private String movieTitle;

    @Schema(description = "상영관 이름", example = "2관")
    private String roomName;

    @Schema(description = "상영 시작 시간 (HH:mm 형식)", example = "10:30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalDateTime screeningStartTime;

    @Schema(description = "상영 종료 시간 (HH:mm 형식)", example = "12:19")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalDateTime screeningEndTime;

    @Schema(description = "예매 가능 좌석 수", example = "118")
    private Long availableSeatsCount;

    @Schema(description = "전체 좌석 수", example = "120")
    private Integer totalSeatsCount;

    public ScreeningScheduleResponseDto(Screening screening) {
        this.screeningId = screening.getId();

        if (screening.getMovie() != null) {
            this.movieTitle = screening.getMovie().getTitle();
            if (screening.getMovie().getDuration() != null) {
                this.screeningEndTime = screening.getScreeningTime().plusMinutes(screening.getMovie().getDuration());
            }
        }

        if (screening.getScreeningRoom() != null) {
            this.roomName = screening.getScreeningRoom().getRoomName();
            this.totalSeatsCount = screening.getScreeningRoom().getCapacity();
        }

        this.screeningStartTime = screening.getScreeningTime();

        if (screening.getScreeningSeats() != null) {
            this.availableSeatsCount = screening.getScreeningSeats().stream()
                    .filter(seat -> seat.getSeatStatus() == SeatStatus.AVAILABLE)
                    .count();
        } else {
            this.availableSeatsCount = 0L;
        }
    }
}