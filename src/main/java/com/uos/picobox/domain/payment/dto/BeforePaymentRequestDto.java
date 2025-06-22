package com.uos.picobox.domain.payment.dto;

import com.uos.picobox.domain.payment.entity.Payment;
import com.uos.picobox.domain.reservation.entity.Reservation;
import com.uos.picobox.global.enumClass.PaymentMethod;
import com.uos.picobox.global.enumClass.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class BeforePaymentRequestDto {
    @NotNull(message = "예약 ID는 필수입니다.")
    @Schema(description = "결제를 완료할 예약 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reservationId;

    @NotBlank(message = "주문 ID는 필수입니다.")
    @Schema(description = "결제 시스템에서 생성된 주문 ID", example = "ORDER_20241221_001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderId;

    @NotNull(message = "결제 수단 정보는 필수입니다.")
    @Schema(description = "결제 수단",
            example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"CARD", "VIRTUAL_ACCOUNT", "EASY_PAY", "GAME_GIFT", "TRANSFER", "BOOK_GIFT", "CULTURE_GIFT", "MOBILE"})
    private PaymentMethod paymentMethod;

    @Schema(description = "결제에 사용된 통화 단위", example = "KRW")
    @NotBlank
    private String currency;

    @Schema(description = "할인 정책 ID (없을 수 있음)", example = "3")
    private Long paymentDiscountId;

    @NotNull(message = "사용할 포인트는 필수입니다. (사용 안 할 시 0)")
    @Min(value = 0, message = "사용 포인트는 0 이상이어야 합니다.")
    @Schema(description = "사용할 포인트 금액", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer usedPointAmount;

    @Schema(description = "총 결제 금액 (포인트와 할인 전)", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer amount;

    @Schema(description = "최종 결제 금액 (포인트와 할인 반영 후)", example = "4000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer finalAmount;

    public Payment toEntity(Reservation reservation) {
        return Payment.builder()
                .reservation(reservation)
                .orderId(this.orderId)
                .paymentMethod(this.paymentMethod)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .currency(this.currency)
                .paymentDiscountId(this.paymentDiscountId)
                .usedPointAmount(this.usedPointAmount)
                .amount(this.amount)
                .finalAmount(this.finalAmount)
                .build();
    }
}
