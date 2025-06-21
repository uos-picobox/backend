package com.uos.picobox.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TICKET")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TICKET_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESERVATION_ID", nullable = false)
    private Reservation reservation;

    @Column(name = "SCREENING_ID", nullable = false)
    private Long screeningId;

    @Column(name = "SEAT_ID", nullable = false)
    private Long seatId;

    @Column(name = "TICKET_TYPE_ID", nullable = false)
    private Long ticketTypeId;

    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @Column(name = "TICKET_STATUS", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    @Builder
    public Ticket(Reservation reservation, Long screeningId, Long seatId, Long ticketTypeId, Integer price, TicketStatus ticketStatus) {
        this.reservation = reservation;
        this.screeningId = screeningId;
        this.seatId = seatId;
        this.ticketTypeId = ticketTypeId;
        this.price = price;
        this.ticketStatus = ticketStatus;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void updateStatus(TicketStatus ticketStatus) {
        this.ticketStatus = ticketStatus;
    }
}