package com.uos.picobox.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ConfirmPaymentRequestDto {
    @Schema(description = "결제 정보 식별자", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long paymentId;

    @NotBlank(message = "주문 ID는 필수입니다.")
    @Schema(description = "결제 시스템에서 생성된 주문 ID", example = "ORDER_20241221_001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderId;

    @NotBlank(message = "결제 키는 필수입니다.")
    @Schema(description = "결제 시스템에서 생성된 결제 키", example = "PAY_KEY_12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentKey;

    @Schema(description = "최종 결제 금액 (포인트와 할인 반영 후)", example = "4000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer finalAmount;
}
