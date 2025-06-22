package com.uos.picobox.domain.movie.repository;

import com.uos.picobox.domain.movie.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    Optional<Actor> findByNameAndBirthDate(String name, java.time.LocalDate birthDate);

    @Query(value = "SELECT m.TITLE FROM MOVIE m " +
            "JOIN MOVIE_CAST mc ON m.MOVIE_ID = mc.MOVIE_ID " +
            "WHERE mc.ACTOR_ID = :actorId", nativeQuery = true)
    List<String> findMovieTitlesByActorId(@Param("actorId") Long actorId);

    /**
     * 배우 이름으로 검색
     */
    @Query("SELECT a FROM Actor a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.name")
    List<Actor> findActorsByNameContaining(@Param("keyword") String keyword);
}