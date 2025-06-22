package com.uos.picobox.domain.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "PAYMENT_DISCOUNT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_DISCOUNT_ID")
    private Long id;

    @Column(name = "PROVIDER_NAME", nullable = false, length = 50)
    private String providerName;

    @Column(name = "DISCOUNT_RATE", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "DISCOUNT_AMOUNT")
    private Integer discountAmount;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;
}