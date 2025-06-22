package com.uos.picobox.domain.payment.dto;

import com.uos.picobox.global.enumClass.PaymentMethod;
import com.uos.picobox.global.enumClass.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ConfirmPaymentResponseDto {
    @Schema(description = "결제 정보 식별자", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long paymentId;

    @Schema(description = "결제를 완료할 예약 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reservationId;

    @Schema(description = "결제 시스템에서 생성된 주문 ID", example = "ORDER_20241221_001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderId;

    @Schema(description = "결제 시스템에서 생성된 결제 키", example = "PAY_KEY_12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentKey;

    @Schema(description = "결제 수단",
            example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"CARD", "VIRTUAL_ACCOUNT", "EASY_PAY", "GAME_GIFT", "TRANSFER", "BOOK_GIFT", "CULTURE_GIFT", "MOBILE"})
    private PaymentMethod paymentMethod;

    @Schema(
            description = "결제 상태",
            example = "DONE",
            allowableValues = {
                    "ABORTED",
                    "DONE",
                    "EXPIRED",
                    "IN_PROGRESS",
                    "PARTIAL_CANCELED",
                    "READY",
                    "WAITING_FOR_DEPOSIT"
            },
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private PaymentStatus paymentStatus;

    @Schema(description = "결제에 사용된 통화 단위", example = "KRW")
    private String currency;

    @Schema(description = "할인 정책 ID (없을 수 있음)", example = "3")
    private Long paymentDiscountId;

    @Schema(description = "사용된 포인트 금액 (없으면 0)", example = "1000")
    private Integer usedPointAmount;

    @Schema(description = "총 결제 금액 (포인트와 할인 전)", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer amount;

    @Schema(description = "최종 결제 금액 (포인트와 할인 반영 후)", example = "4000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer finalAmount;

    @Schema(description = "결제가 승인된 시각", example = "2025-06-22T14:33:00")
    private LocalDateTime approvedAt;

    @Schema(description = "결제가 요청된 시각", example = "2025-06-22T14:30:00")
    private LocalDateTime requestedAt;
}
