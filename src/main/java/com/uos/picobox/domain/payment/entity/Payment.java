package com.uos.picobox.domain.payment.entity;

import com.uos.picobox.domain.reservation.entity.Reservation;
import com.uos.picobox.global.converter.PaymentMethodConverter;
import com.uos.picobox.global.converter.PaymentStatusConverter;
import com.uos.picobox.global.enumClass.PaymentMethod;
import com.uos.picobox.global.enumClass.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESERVATION_ID", nullable = false)
    private Reservation reservation;

    @Column(name = "ORDER_ID", nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(name = "PAYMENT_KEY", nullable = false, unique = true, length = 100)
    private String paymentKey;

    @Column(name = "PAYMENT_METHOD", nullable = false, length = 30)
    @Convert(converter = PaymentMethodConverter.class)
    private PaymentMethod paymentMethod;

    @Column(name = "PAYMENT_STATUS", nullable = false, length = 30)
    @Convert(converter = PaymentStatusConverter.class)
    private PaymentStatus paymentStatus;

    @Column(name = "CURRENCY", nullable = false, length = 10)
    private String currency = "KRW";

    @Column(name = "PAYMENT_DISCOUNT_ID")
    private Long paymentDiscountId;

    @Column(name = "USED_POINT_AMOUNT", nullable = false)
    private Integer usedPointAmount = 0;

    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;

    @Column(name = "FINAL_AMOUNT", nullable = false)
    private Integer finalAmount;

    @Column(name = "APPROVED_AT")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "REQUESTED_AT", nullable = false)
    private LocalDateTime requestedAt;

    @Builder
    public Payment(Reservation reservation, String orderId, String paymentKey, 
                   PaymentMethod paymentMethod, PaymentStatus paymentStatus, String currency,
                   Long paymentDiscountId, Integer usedPointAmount, Integer amount, 
                   Integer finalAmount, LocalDateTime approvedAt) {
        this.reservation = reservation;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.currency = currency != null ? currency : "KRW";
        this.paymentDiscountId = paymentDiscountId;
        this.usedPointAmount = usedPointAmount != null ? usedPointAmount : 0;
        this.amount = amount;
        this.finalAmount = finalAmount;
        this.approvedAt = approvedAt;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void updateStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void approve() {
        this.paymentStatus = PaymentStatus.DONE;
        this.approvedAt = LocalDateTime.now();
    }
}