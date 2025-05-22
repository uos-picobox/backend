package com.uos.picobox.domain.movie.repository;

import com.uos.picobox.domain.movie.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    Optional<Actor> findByNameAndBirthDate(String name, java.time.LocalDate birthDate);
}