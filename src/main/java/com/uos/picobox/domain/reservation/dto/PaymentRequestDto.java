package com.uos.picobox.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentRequestDto {
    @NotNull(message = "예약 ID는 필수입니다.")
    @Schema(description = "결제를 완료할 예약 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reservationId;

    @NotBlank(message = "주문 ID는 필수입니다.")
    @Schema(description = "결제 시스템에서 생성된 주문 ID", example = "ORDER_20241221_001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderId;

    @NotBlank(message = "결제 키는 필수입니다.")
    @Schema(description = "결제 시스템에서 생성된 결제 키", example = "PAY_KEY_12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentKey;

    @NotBlank(message = "결제 수단 정보는 필수입니다.")
    @Schema(description = "결제 수단", 
            example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"CARD", "VIRTUAL_ACCOUNT", "EASY_PAY", "GAME_GIFT", "TRANSFER", "BOOK_GIFT", "CULTURE_GIFT", "MOBILE"})
    private String paymentMethod;

    @Schema(description = "사용된 포인트 금액", example = "1000")
    private Integer usedPointAmount = 0;
}