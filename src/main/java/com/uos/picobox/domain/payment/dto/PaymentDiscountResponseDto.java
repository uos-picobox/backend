package com.uos.picobox.domain.payment.dto;

import com.uos.picobox.domain.payment.entity.PaymentDiscount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "결제 할인 정보 DTO")
public class PaymentDiscountResponseDto {

    @Schema(description = "결제 할인 ID", example = "1")
    private Long id;

    @Schema(description = "할인 제공자 이름", example = "LUDEX")
    private String providerName;

    @Schema(description = "할인율 (단위: %). 할인율과 할인금액 중 하나만 설정해야 함", example = "15.00")
    private BigDecimal discountRate;

    @Schema(description = "할인 금액 (단위: 원). 할인율과 할인금액 중 하나만 설정해야 함", example = "3000")
    private Integer discountAmount;

    @Schema(description = "할인 설명", example = "여름 한정 특별 할인")
    private String description;

    public PaymentDiscountResponseDto(PaymentDiscount discount) {
        this.id = discount.getId();
        this.providerName = discount.getProviderName();
        this.discountRate = discount.getDiscountRate();
        this.discountAmount = discount.getDiscountAmount();
        this.description = discount.getDescription();
    }
}
