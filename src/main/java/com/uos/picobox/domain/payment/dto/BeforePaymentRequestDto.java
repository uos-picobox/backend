package com.uos.picobox.domain.payment.dto;

import com.uos.picobox.global.enumClass.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

    @NotBlank(message = "결제 수단 정보는 필수입니다.")
    @Schema(description = "결제 수단",
            example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"CARD", "VIRTUAL_ACCOUNT", "EASY_PAY", "GAME_GIFT", "TRANSFER", "BOOK_GIFT", "CULTURE_GIFT", "MOBILE"})
    private PaymentMethod paymentMethod;

    @Schema(description = "할인 정책 ID (없을 수 있음)", example = "3")
    private Long paymentDiscountId;

    @Schema(description = "사용된 포인트 금액 (없으면 0)", example = "1000")
    @NotNull
    private Integer usedPointAmount;

    @Schema(description = "총 결제 금액 (포인트와 할인 전)", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer amount;

    @Schema(description = "최종 결제 금액 (포인트와 할인 반영 후)", example = "4000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer finalAmount;
}
