package com.uos.picobox.domain.reservation.repository;

import com.uos.picobox.domain.reservation.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}