package com.uos.picobox.domain.ticket.repository;

import com.uos.picobox.domain.ticket.entity.Ticket;
import com.uos.picobox.global.enumClass.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    /**
     * 특정 상영의 특정 상태 티켓들을 조회합니다.
     * @param screeningId 상영 ID
     * @param ticketStatus 티켓 상태
     * @return 해당 조건의 티켓 목록
     */
    List<Ticket> findByScreeningIdAndTicketStatus(Long screeningId, TicketStatus ticketStatus);
}