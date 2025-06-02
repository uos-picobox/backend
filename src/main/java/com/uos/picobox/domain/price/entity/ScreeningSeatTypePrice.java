package com.uos.picobox.domain.price.entity;

import com.uos.picobox.domain.room.entity.ScreeningRoom;
import com.uos.picobox.domain.ticket.entity.TicketType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "SCREENING_SEAT_TYPE_PRICE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ScreeningSeatTypePrice.ScreeningSeatTypePriceId.class)
public class ScreeningSeatTypePrice {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID", nullable = false)
    private ScreeningRoom screeningRoom;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKET_TYPE_ID", nullable = false)
    private TicketType ticketType;

    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @Builder
    public ScreeningSeatTypePrice(ScreeningRoom screeningRoom, TicketType ticketType, Integer price) {
        this.screeningRoom = screeningRoom;
        this.ticketType = ticketType;
        this.price = price;
    }

    public void updatePrice(Integer price) {
        this.price = price;
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ScreeningSeatTypePriceId implements Serializable {
        private Long screeningRoom;
        private Long ticketType;
    }
}