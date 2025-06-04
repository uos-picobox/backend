package com.uos.picobox.domain.room.repository;

import com.uos.picobox.domain.room.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Modifying
    @Query("DELETE FROM Seat s WHERE s.screeningRoom.id = :roomId")
    void deleteAllByScreeningRoomId(@Param("roomId") Long roomId);
}