package com.uos.picobox.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReservationDetailResponseDto {

    // 예매 기본 정보
    @Schema(description = "예약 ID", example = "1")
    private final Long reservationId;

    @Schema(description = "예매 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime reservationDate;

    @Schema(description = "결제 상태", example = "COMPLETED")
    private final String paymentStatus;

    // 영화 및 상영 정보
    @Schema(description = "영화 제목", example = "기생충")
    private final String movieTitle;

    @Schema(description = "영화 포스터 URL")
    private final String posterUrl;

    @Schema(description = "영화 등급", example = "15세 이상 관람가")
    private final String movieRating;

    @Schema(description = "상영 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime screeningTime;

    @Schema(description = "상영 종료 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime screeningEndTime;

    @Schema(description = "상영관 이름", example = "2관")
    private final String screeningRoomName;

    @Schema(description = "좌석 번호 목록", example = "['A1', 'A2']")
    private final List<String> seatNumbers;

    // 결제 정보
    @Schema(description = "총 티켓 금액 (할인 전)", example = "30000")
    private final Integer totalAmount;

    @Schema(description = "사용한 포인트", example = "1000")
    private final Integer usedPoints;

    @Schema(description = "최종 결제 금액", example = "29000")
    private final Integer finalAmount;

    @Schema(description = "결제 방법", example = "CARD")
    private final String paymentMethod;

    @Schema(description = "결제 완료 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime paymentCompletedAt;

    public ReservationDetailResponseDto(Long reservationId, LocalDateTime reservationDate, String paymentStatus,
                                       String movieTitle, String posterUrl, String movieRating,
                                       LocalDateTime screeningTime, LocalDateTime screeningEndTime, String screeningRoomName,
                                       List<String> seatNumbers, Integer totalAmount, Integer usedPoints, Integer finalAmount,
                                       String paymentMethod, LocalDateTime paymentCompletedAt) {
        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.paymentStatus = paymentStatus;
        this.movieTitle = movieTitle;
        this.posterUrl = posterUrl;
        this.movieRating = movieRating;
        this.screeningTime = screeningTime;
        this.screeningEndTime = screeningEndTime;
        this.screeningRoomName = screeningRoomName;
        this.seatNumbers = seatNumbers;
        this.totalAmount = totalAmount;
        this.usedPoints = usedPoints;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentCompletedAt = paymentCompletedAt;
    }
} 