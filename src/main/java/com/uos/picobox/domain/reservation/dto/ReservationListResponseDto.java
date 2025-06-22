package com.uos.picobox.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReservationListResponseDto {

    @Schema(description = "예약 ID", example = "1")
    private final Long reservationId;

    @Schema(description = "영화 제목", example = "기생충")
    private final String movieTitle;

    @Schema(description = "영화 포스터 URL")
    private final String posterUrl;

    @Schema(description = "상영 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime screeningTime;

    @Schema(description = "상영관 이름", example = "2관")
    private final String screeningRoomName;

    @Schema(description = "좌석 번호 목록", example = "['A1', 'A2']")
    private final List<String> seatNumbers;

    @Schema(description = "예매 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime reservationDate;

    @Schema(description = "예매 상태", example = "COMPLETED")
    private final String reservationStatus;

    @Schema(description = "결제 상태", example = "COMPLETED")
    private final String paymentStatus;

    @Schema(description = "총 결제 금액", example = "29000")
    private final Integer finalAmount;

    @Schema(description = "상영 완료 여부", example = "false")
    private final Boolean isScreeningCompleted;

    public ReservationListResponseDto(Long reservationId, String movieTitle, String posterUrl, 
                                    LocalDateTime screeningTime, String screeningRoomName, 
                                    List<String> seatNumbers, LocalDateTime reservationDate, 
                                    String reservationStatus, String paymentStatus, Integer finalAmount, Boolean isScreeningCompleted) {
        this.reservationId = reservationId;
        this.movieTitle = movieTitle;
        this.posterUrl = posterUrl;
        this.screeningTime = screeningTime;
        this.screeningRoomName = screeningRoomName;
        this.seatNumbers = seatNumbers;
        this.reservationDate = reservationDate;
        this.reservationStatus = reservationStatus;
        this.paymentStatus = paymentStatus;
        this.finalAmount = finalAmount;
        this.isScreeningCompleted = isScreeningCompleted;
    }
} 