package com.uos.picobox.domain.movie.repository;

import com.uos.picobox.domain.movie.entity.Distributor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    Optional<Distributor> findByName(String name);
}