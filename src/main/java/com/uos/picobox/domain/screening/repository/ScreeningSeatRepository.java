package com.uos.picobox.domain.screening.repository;

import com.uos.picobox.domain.screening.entity.ScreeningSeat;
import com.uos.picobox.domain.screening.entity.ScreeningSeat.ScreeningSeatId;
import com.uos.picobox.domain.screening.entity.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface ScreeningSeatRepository extends JpaRepository<ScreeningSeat, ScreeningSeatId> {

    // 특정 스크리닝의 모든 좌석 삭제 (ScreeningService의 editScreening에서 상영관 변경 시 사용)
    @Modifying
    @Query("DELETE FROM ScreeningSeat ss WHERE ss.screening.id = :screeningId")
    void deleteAllByScreeningId(@Param("screeningId") Long screeningId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ScreeningSeat ss WHERE ss.screening.id = :screeningId AND ss.seat.id = :seatId")
    Optional<ScreeningSeat> findByIdWithPessimisticLock(@Param("screeningId") Long screeningId, @Param("seatId") Long seatId);

    List<ScreeningSeat> findAllBySeatStatusAndHoldExpiresAtBefore(SeatStatus seatStatus, LocalDateTime now);
}