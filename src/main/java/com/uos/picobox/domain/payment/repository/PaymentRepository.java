package com.uos.picobox.domain.payment.repository;

import com.uos.picobox.domain.payment.entity.Payment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.reservation.id = :reservationId ")
    Optional<Payment> findByReservationId(@Param("reservationId") Long reservationId);

}