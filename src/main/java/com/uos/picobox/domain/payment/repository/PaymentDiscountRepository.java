package com.uos.picobox.domain.payment.repository;

import com.uos.picobox.domain.payment.entity.PaymentDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDiscountRepository extends JpaRepository<PaymentDiscount, Long> {
}
