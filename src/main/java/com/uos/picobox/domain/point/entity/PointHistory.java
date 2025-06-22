package com.uos.picobox.domain.point.entity;

import com.uos.picobox.global.enumClass.PointChangeType;
import com.uos.picobox.user.entity.Customer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "POINT_HISTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POINT_HISTORY_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @Column(name = "CHANGE_TYPE", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PointChangeType changeType;

    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;

    @Column(name = "RELATED_RESERVATION_ID")
    private Long relatedReservationId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public PointHistory(Customer customer, PointChangeType changeType, Integer amount, Long relatedReservationId) {
        this.customer = customer;
        this.changeType = changeType;
        this.amount = amount;
        this.relatedReservationId = relatedReservationId;
        this.createdAt = LocalDateTime.now();
    }
}