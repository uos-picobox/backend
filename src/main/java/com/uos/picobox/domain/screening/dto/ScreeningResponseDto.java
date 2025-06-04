package com.uos.picobox.domain.screening.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uos.picobox.domain.screening.entity.Screening;
import com.uos.picobox.domain.screening.entity.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ScreeningResponseDto {

    @Schema(description = "상영 스케줄 ID", example = "1")
    private Long screeningId;

    @Schema(description = "영화 정보")
    private SimpleMovieDto movie;

    @Schema(description = "상영관 정보")
    private SimpleScreeningRoomDto screeningRoom;

    @Schema(description = "상영 날짜 (yyyy-MM-dd)", example = "2025-07-15")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate screeningDate;

    @Schema(description = "회차", example = "1")
    private Integer screeningSequence;

    @Schema(description = "상영 시작 시간 (yyyy-MM-dd HH:mm:ss)", example = "2025-07-15 10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime screeningTime;

    @Schema(description = "해당 상영의 총 좌석 수", example = "120")
    private Integer totalSeats;

    @Schema(description = "해당 상영의 현재 예매 가능 좌석 수", example = "118")
    private Long availableSeats;


    public ScreeningResponseDto(Screening screening) {
        this.screeningId = screening.getId();
        if (screening.getMovie() != null) {
            this.movie = new SimpleMovieDto(screening.getMovie().getId(), screening.getMovie().getTitle());
        }
        if (screening.getScreeningRoom() != null) {
            this.screeningRoom = new SimpleScreeningRoomDto(screening.getScreeningRoom().getId(), screening.getScreeningRoom().getRoomName());
            this.totalSeats = screening.getScreeningRoom().getCapacity();
        }
        this.screeningDate = screening.getScreeningDate();
        this.screeningSequence = screening.getScreeningSequence();
        this.screeningTime = screening.getScreeningTime();

        // 사용 가능한 좌석 수 계산
        if (screening.getScreeningSeats() != null) {
            if(this.totalSeats == null && screening.getScreeningRoom() == null) {
                this.totalSeats = screening.getScreeningSeats().size();
            }
            this.availableSeats = screening.getScreeningSeats().stream()
                    .filter(seat -> SeatStatus.AVAILABLE == seat.getSeatStatus())
                    .count();
        } else {
            this.availableSeats = 0L;
            if(this.totalSeats == null) this.totalSeats = 0;
        }
    }

    @Getter
    private static class SimpleMovieDto {
        @Schema(description = "영화 ID")
        private Long movieId;
        @Schema(description = "영화 제목")
        private String title;
        public SimpleMovieDto(Long movieId, String title) {
            this.movieId = movieId;
            this.title = title;
        }
    }

    @Getter
    private static class SimpleScreeningRoomDto {
        @Schema(description = "상영관 ID")
        private Long roomId;
        @Schema(description = "상영관 이름")
        private String roomName;
        public SimpleScreeningRoomDto(Long roomId, String roomName) {
            this.roomId = roomId;
            this.roomName = roomName;
        }
    }
}