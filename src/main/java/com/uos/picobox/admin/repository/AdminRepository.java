package com.uos.picobox.admin.repository;

import com.uos.picobox.admin.entity.Admin;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    @Query("SELECT a.password FROM Admin a WHERE a.loginId = :loginId")
    String findPasswordByLoginId(@Param("loginId") String loginId);

    @Modifying
    @Query("DELETE FROM Admin a WHERE a.id = :id")
    void deleteById(@NonNull @Param("id") Long id);

    @Query("SELECT a.id FROM Admin a WHERE a.loginId = :loginId")
    Long findIdByLoginId(@Param("loginId") String loginId);

}
