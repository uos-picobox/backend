package com.uos.picobox.user.repository;

import com.uos.picobox.user.entity.Guest;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    boolean existsByEmail(String email);
    @Query("SELECT g.password FROM Guest g WHERE g.email = :email")
    String findPasswordByEmail(@Param("email") String email);
    @Modifying
    @Query("DELETE FROM Guest g WHERE g.expirationDate < :threshold ")
    void deleteExpiredGuests(@Param("threshold") LocalDateTime threshold);
    @Query("SELECT g.id FROM Guest g WHERE g.email = :email")
    Long findIdByEmail(@Param("email") String email);
}
