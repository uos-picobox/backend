package com.uos.picobox.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CancelReservationRequestDto {
    @Schema(description = "예매 ID", example = "123")
    @NotNull
    private Long reservationId;

    @Schema(description = "환불 사유", example = "사용자 단순 변심")
    @NotBlank
    private String refundReason;
}
