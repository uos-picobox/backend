package com.uos.picobox.admin.repository;

import com.uos.picobox.admin.entity.Admin;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    @Query("SELECT a.password FROM Admin a WHERE a.loginId = :loginId")
    String findPasswordByLoginId(@Param("loginId") String loginId);
}
