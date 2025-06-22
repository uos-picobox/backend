package com.uos.picobox.domain.payment.dto;

import com.uos.picobox.domain.payment.entity.PaymentDiscount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class PaymentDiscountRequestDto {

    @Schema(description = "할인 제공자 이름", example = "PicoBox")
    @NotBlank
    private String providerName;

    @Schema(description = "할인율 (단위: %). 할인율과 할인금액 중 하나만 설정해야 함", example = "15.00")
    private BigDecimal discountRate;

    @Schema(description = "할인 금액 (단위: 원). 할인율과 할인금액 중 하나만 설정해야 함", example = "3000")
    private Integer discountAmount;

    @Schema(description = "할인 설명", example = "여름 한정 특별 할인")
    @NotBlank
    private String description;

    public PaymentDiscount toEntity() {
        return PaymentDiscount.builder()
                .providerName(this.providerName)
                .discountRate(this.discountRate)
                .discountAmount(this.discountAmount)
                .description(this.description)
                .build();

    }
}
