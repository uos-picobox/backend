package com.uos.picobox.domain.reservation.repository;

import com.uos.picobox.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomerIdOrderByReservationDateDesc(Long customerId);
    boolean existsByScreeningId(Long screeningId);
}