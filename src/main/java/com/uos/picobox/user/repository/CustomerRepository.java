package com.uos.picobox.user.repository;

import com.uos.picobox.user.entity.Customer;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    @Query("SELECT c.password FROM Customer c WHERE c.loginId = :loginId")
    String findPasswordByLoginId(@Param("loginId") String loginId);
    @Query("SELECT c.id FROM Customer c WHERE c.loginId = :loginId")
    Long findIdByLoginId(@Param("loginId") String loginId);
    @NonNull
    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    Optional<Customer> findById(@Param("id") @NonNull Long id);
    
    // 관리자용 회원 관리 메서드들
    List<Customer> findByIsActive(Boolean isActive);
    List<Customer> findByLoginIdContainingIgnoreCase(String loginId);
    List<Customer> findByNameContainingIgnoreCase(String name);
    List<Customer> findByEmailContainingIgnoreCase(String email);
}
