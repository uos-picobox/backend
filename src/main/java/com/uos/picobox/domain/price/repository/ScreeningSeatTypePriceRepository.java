package com.uos.picobox.domain.price.repository;

import com.uos.picobox.domain.price.entity.ScreeningSeatTypePrice;
import com.uos.picobox.domain.price.entity.ScreeningSeatTypePrice.ScreeningSeatTypePriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScreeningSeatTypePriceRepository extends JpaRepository<ScreeningSeatTypePrice, ScreeningSeatTypePriceId> {

    @Query("SELECT CASE WHEN COUNT(p.screeningRoom.id) > 0 THEN true ELSE false END FROM ScreeningSeatTypePrice p WHERE p.ticketType.id = :ticketTypeId")
    boolean existsByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);

    @Query("SELECT p FROM ScreeningSeatTypePrice p JOIN FETCH p.screeningRoom sr JOIN FETCH p.ticketType tt WHERE sr.id = :roomId")
    List<ScreeningSeatTypePrice> findByScreeningRoomIdWithDetails(@Param("roomId") Long roomId);
}