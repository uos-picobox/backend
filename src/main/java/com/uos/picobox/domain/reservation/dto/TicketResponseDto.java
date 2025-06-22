package com.uos.picobox.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TicketResponseDto {

    @Schema(description = "예매 ID", example = "1")
    private final Long reservationId;

    // 영화 정보
    @Schema(description = "영화 제목", example = "기생충")
    private final String movieTitle;

    @Schema(description = "영화 포스터 URL")
    private final String posterUrl;

    @Schema(description = "관람 등급", example = "15세 이상 관람가")
    private final String movieRating;

    // 상영 정보
    @Schema(description = "상영 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 (E) HH:mm")
    private final LocalDateTime screeningStartTime;

    @Schema(description = "상영 종료 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private final LocalDateTime screeningEndTime;

    @Schema(description = "상영관", example = "2관")
    private final String screeningRoom;

    @Schema(description = "좌석", example = "A1, A2")
    private final String seats;

    @Schema(description = "인원수", example = "2명")
    private final Integer peopleCount;

    // 예매 정보
    @Schema(description = "예매자", example = "김철수")
    private final String reserverName;

    @Schema(description = "예매 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    private final LocalDateTime reservationDate;

    public TicketResponseDto(Long reservationId, String movieTitle, String posterUrl,
                           String movieRating, LocalDateTime screeningStartTime, LocalDateTime screeningEndTime,
                           String screeningRoom, String seats, Integer peopleCount, String reserverName,
                           LocalDateTime reservationDate) {
        this.reservationId = reservationId;
        this.movieTitle = movieTitle;
        this.posterUrl = posterUrl;
        this.movieRating = movieRating;
        this.screeningStartTime = screeningStartTime;
        this.screeningEndTime = screeningEndTime;
        this.screeningRoom = screeningRoom;
        this.seats = seats;
        this.peopleCount = peopleCount;
        this.reserverName = reserverName;
        this.reservationDate = reservationDate;
    }
} 