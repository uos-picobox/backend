package com.uos.picobox.domain.room.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SCREENING_ROOM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScreeningRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROOM_ID")
    private Long id;

    @Column(name = "ROOM_NAME", nullable = false, length = 50)
    private String roomName;

    @Column(name = "CAPACITY", nullable = false)
    private Integer capacity;

    @OneToMany(mappedBy = "screeningRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();

    @Builder
    public ScreeningRoom(String roomName, Integer capacity) {
        this.roomName = roomName;
        this.capacity = capacity;
    }

    public void updateRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void updateCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void clearSeats() {
        for (Seat seat : new ArrayList<>(this.seats)) {
            removeSeat(seat);
        }
    }

    private void removeSeat(Seat seat) {
        this.seats.remove(seat);
        seat.setScreeningRoom(null);
    }

    public void addSeat(Seat seat) {
        this.seats.add(seat);
        seat.setScreeningRoom(this);
    }

    public void setSeats(List<Seat> newSeats) {
        this.clearSeats();
        if (newSeats != null) {
            for (Seat seat : newSeats) {
                this.addSeat(seat);
            }
        }
    }
}