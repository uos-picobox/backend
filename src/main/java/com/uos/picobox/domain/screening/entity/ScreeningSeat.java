package com.uos.picobox.domain.screening.entity;

import com.uos.picobox.domain.room.entity.Seat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "SCREENING_SEAT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ScreeningSeat.ScreeningSeatId.class)
public class ScreeningSeat {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCREENING_ID", nullable = false)
    @Setter
    private Screening screening;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEAT_ID", nullable = false)
    @Setter
    private Seat seat;

    @Column(name = "SEAT_STATUS", nullable = false, length = 15)
    @Setter
    @Enumerated(EnumType.STRING)
    private SeatStatus seatStatus;

    @Setter
    @Column(name = "HOLD_EXPIRES_AT")
    private LocalDateTime holdExpiresAt;

    @Setter
    @Column(name = "HOLD_CUSTOMER_ID")
    private Long holdCustomerId;

    @Setter
    @Column(name = "HOLD_GUEST_ID")
    private Long holdGuestId;

    @Builder
    public ScreeningSeat(Screening screening, Seat seat, SeatStatus seatStatus) {
        this.screening = screening;
        this.seat = seat;
        this.seatStatus = seatStatus;
    }

    // 복합키 클래스
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ScreeningSeatId implements Serializable {
        private Long screening;
        private Long seat;

        public ScreeningSeatId(Long screeningId, Long seatId) {
            this.screening = screeningId;
            this.seat = seatId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScreeningSeatId that = (ScreeningSeatId) o;
            return Objects.equals(screening, that.screening) && Objects.equals(seat, that.seat);
        }

        @Override
        public int hashCode() {
            return Objects.hash(screening, seat);
        }
    }
}

