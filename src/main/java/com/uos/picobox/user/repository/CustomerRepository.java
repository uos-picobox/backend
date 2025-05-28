package com.uos.picobox.user.repository;

import com.uos.picobox.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
}
