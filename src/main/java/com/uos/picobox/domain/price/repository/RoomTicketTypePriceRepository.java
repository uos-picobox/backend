package com.uos.picobox.domain.price.repository;

import com.uos.picobox.domain.price.entity.RoomTicketTypePrice;
import com.uos.picobox.domain.price.entity.RoomTicketTypePrice.RoomTicketTypePriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomTicketTypePriceRepository extends JpaRepository<RoomTicketTypePrice, RoomTicketTypePriceId> {

    @Query("SELECT CASE WHEN COUNT(p.screeningRoom.id) > 0 THEN true ELSE false END FROM RoomTicketTypePrice p WHERE p.ticketType.id = :ticketTypeId")
    boolean existsByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);

    @Query("SELECT p FROM RoomTicketTypePrice p JOIN FETCH p.screeningRoom sr JOIN FETCH p.ticketType tt WHERE sr.id = :roomId")
    List<RoomTicketTypePrice> findByScreeningRoomIdWithDetails(@Param("roomId") Long roomId);
}