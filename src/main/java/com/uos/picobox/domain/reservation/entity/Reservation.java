package com.uos.picobox.domain.reservation.entity;

import com.uos.picobox.domain.payment.entity.Payment;
import com.uos.picobox.domain.ticket.entity.Ticket;
import com.uos.picobox.global.converter.PaymentStatusConverter;
import com.uos.picobox.global.enumClass.PaymentStatus;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.entity.Guest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RESERVATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESERVATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GUEST_ID")
    private Guest guest;

    @Column(name = "SCREENING_ID", nullable = false)
    private Long screeningId;

    @Column(name = "RESERVATION_DATE", nullable = false)
    private LocalDateTime reservationDate;

    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Integer totalAmount;

    @Column(name = "PAYMENT_STATUS", nullable = false, length = 15)
    @Convert(converter = PaymentStatusConverter.class)
    private PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Payment payment;

    @Builder
    public Reservation(Customer customer, Guest guest, Long screeningId, Integer totalAmount, PaymentStatus paymentStatus) {
        this.customer = customer;
        this.guest = guest;
        this.screeningId = screeningId;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.reservationDate = LocalDateTime.now();
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null && payment.getReservation() != this) {
            payment.setReservation(this);
        }
    }

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
        if (ticket.getReservation() != this) {
            ticket.setReservation(this);
        }
    }

    public void updatePaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}