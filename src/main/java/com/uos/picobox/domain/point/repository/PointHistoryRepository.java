package com.uos.picobox.domain.point.repository;

import com.uos.picobox.domain.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.repository.query.Param;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    @Query("SELECT ph FROM PointHistory ph WHERE ph.customer.id = :id")
    List<PointHistory> findAllByCustomerId(@Param("id") Long id);
}