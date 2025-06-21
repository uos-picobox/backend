package com.uos.picobox.domain.reservation.repository;

import com.uos.picobox.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomerIdOrderByReservationDateDesc(Long customerId);
    boolean existsByScreeningId(Long screeningId);
}