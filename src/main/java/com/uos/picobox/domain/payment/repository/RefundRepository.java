package com.uos.picobox.domain.payment.repository;

import com.uos.picobox.domain.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
