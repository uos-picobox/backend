package com.uos.picobox.domain.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "REFUND")
@Getter
@NoArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REFUND_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAYMENT_ID", nullable = false)
    private Payment payment;

    @Column(name = "REFUND_AMOUNT", nullable = false)
    private Integer refundAmount;

    @Column(name = "REFUND_REASON", length = 200)
    private String refundReason;

    @Column(name = "REFUNDED_AT", nullable = false)
    private LocalDateTime refundedAt;

    @Builder
    public Refund(Payment payment, Integer refundAmount, String refundReason) {
        this.payment = payment;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundedAt = LocalDateTime.now();
    }
}