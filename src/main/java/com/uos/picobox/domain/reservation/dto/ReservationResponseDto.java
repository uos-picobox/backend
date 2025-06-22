package com.uos.picobox.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uos.picobox.domain.reservation.entity.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ReservationResponseDto {

    @Schema(description = "예약 ID", example = "1")
    private final Long reservationId;

    @Schema(description = "결제 상태", example = "PENDING")
    private final String paymentStatus;

    @Schema(description = "총 티켓 금액 (할인 전)", example = "30000")
    private final Integer totalAmount;

    @Schema(description = "사용 포인트", example = "1000")
    private final Integer usedPoints;

    @Schema(description = "최종 결제 필요 금액", example = "29000")
    private final Integer finalAmount;

    @Schema(description = "예약(결제 대기) 생성 시각")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @Schema(description = "영화 제목")
    private final String movieTitle; // 필요에 따라 추가

    @Schema(description = "선택한 좌석 목록")
    private final List<String> seatNumbers; // 필요에 따라 추가

    @Builder
    public ReservationResponseDto(Reservation reservation, int usedPoints, int finalAmount, String movieTitle, List<String> seatNumbers) {
        this.reservationId = reservation.getId();
        this.paymentStatus = reservation.getPaymentStatus().name();
        this.totalAmount = reservation.getTotalAmount();
        this.usedPoints = usedPoints;
        this.finalAmount = finalAmount;
        this.createdAt = reservation.getReservationDate();
        this.movieTitle = movieTitle;
        this.seatNumbers = seatNumbers;
    }
}