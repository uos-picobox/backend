package com.uos.picobox.domain.screening.entity;

import com.uos.picobox.domain.movie.entity.Movie;
import com.uos.picobox.domain.room.entity.ScreeningRoom;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SCREENING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCREENING_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOVIE_ID", nullable = false)
    @Setter
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID", nullable = false)
    @Setter
    private ScreeningRoom screeningRoom;

    @Column(name = "SCREENING_DATE", nullable = false)
    private LocalDate screeningDate;

    @Column(name = "SCREENING_SEQUENCE", nullable = false)
    private Integer screeningSequence;

    @Column(name = "SCREENING_TIME", nullable = false)
    private LocalDateTime screeningTime;

    @OneToMany(mappedBy = "screening", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScreeningSeat> screeningSeats = new ArrayList<>();

    @Builder
    public Screening(Movie movie, ScreeningRoom screeningRoom, LocalDateTime screeningTime, LocalDate screeningDate, Integer screeningSequence) {
        this.movie = movie;
        this.screeningRoom = screeningRoom;
        this.screeningTime = screeningTime;
        this.screeningDate = screeningDate;
        this.screeningSequence = screeningSequence;
    }

    public void updateScreeningTimeAndDate(LocalDateTime screeningTime) {
        this.screeningTime = screeningTime;
        this.screeningDate = screeningTime.toLocalDate();
    }

    public void updateScreeningSequence(Integer screeningSequence) {
        this.screeningSequence = screeningSequence;
    }

    public void addScreeningSeat(ScreeningSeat screeningSeat) {
        this.screeningSeats.add(screeningSeat);
        screeningSeat.setScreening(this);
    }

    public void clearScreeningSeats() {
        for (ScreeningSeat seat : new ArrayList<>(this.screeningSeats)) {
            removeScreeningSeat(seat);
        }
    }

    private void removeScreeningSeat(ScreeningSeat screeningSeat) {
        this.screeningSeats.remove(screeningSeat);
        screeningSeat.setScreening(null);
    }

    public void replaceScreeningSeats(List<ScreeningSeat> newSeats) {
        this.clearScreeningSeats();
        if (newSeats != null) {
            for (ScreeningSeat seat : newSeats) {
                this.addScreeningSeat(seat);
            }
        }
    }
}