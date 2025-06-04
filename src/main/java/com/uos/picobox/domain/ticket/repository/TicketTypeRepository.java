package com.uos.picobox.domain.ticket.repository;

import com.uos.picobox.domain.ticket.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    Optional<TicketType> findByTypeName(String typeName);
}