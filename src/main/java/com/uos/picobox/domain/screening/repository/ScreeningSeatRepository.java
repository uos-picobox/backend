package com.uos.picobox.domain.screening.repository;

import com.uos.picobox.domain.screening.entity.ScreeningSeat;
import com.uos.picobox.domain.screening.entity.ScreeningSeat.ScreeningSeatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ScreeningSeatRepository extends JpaRepository<ScreeningSeat, ScreeningSeatId> {

    // 특정 스크리닝의 모든 좌석 삭제 (ScreeningService의 editScreening에서 상영관 변경 시 사용)
    @Modifying
    @Query("DELETE FROM ScreeningSeat ss WHERE ss.screening.id = :screeningId")
    void deleteAllByScreeningId(@Param("screeningId") Long screeningId);
}