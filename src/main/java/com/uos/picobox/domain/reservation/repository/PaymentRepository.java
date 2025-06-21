package com.uos.picobox.domain.reservation.repository;

import com.uos.picobox.domain.reservation.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}