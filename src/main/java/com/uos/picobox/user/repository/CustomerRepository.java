package com.uos.picobox.user.repository;

import com.uos.picobox.user.entity.Customer;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    @Query("SELECT c.password FROM Customer c WHERE c.loginId = :loginId")
    String findPasswordByLoginId(@Param("loginId") String loginId);
    
    Optional<Customer> findByLoginId(String loginId);
}
