package com.uos.picobox.domain.room.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SEAT",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_SEAT_ROOM_NUMBER", columnNames = {"ROOM_ID", "SEAT_NUMBER"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEAT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID", nullable = false)
    @Setter
    private ScreeningRoom screeningRoom;

    @Column(name = "SEAT_NUMBER", nullable = false, length = 10)
    private String seatNumber;

    @Builder
    public Seat(ScreeningRoom screeningRoom, String seatNumber) {
        this.screeningRoom = screeningRoom;
        this.seatNumber = seatNumber;
    }
}