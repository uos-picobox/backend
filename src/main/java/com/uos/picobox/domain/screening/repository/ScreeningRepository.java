package com.uos.picobox.domain.screening.repository;

import com.uos.picobox.domain.screening.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    /**
     * 특정 상영관의 특정 날짜에 예정된 모든 상영 스케줄을 조회합니다.
     * (겹침 확인 및 회차 재정렬 시 사용 가능)
     * @param roomId 상영관 ID
     * @param screeningDate 상영 날짜
     * @return 해당 날짜, 해당 상영관의 스케줄 목록 (영화 정보 포함)
     */
    @Query("SELECT s FROM Screening s JOIN FETCH s.movie m WHERE s.screeningRoom.id = :roomId AND s.screeningDate = :screeningDate ORDER BY s.screeningTime ASC")
    List<Screening> findByScreeningRoomIdAndScreeningDateOrderByScreeningTimeAsc(
            @Param("roomId") Long roomId,
            @Param("screeningDate") LocalDate screeningDate
    );

    /**
     * 특정 상영관 ID를 참조하는 Screening 레코드가 있는지 확인합니다.
     * @param roomId 상영관 ID
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByScreeningRoomId(Long roomId);

    @Query("SELECT s FROM Screening s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screeningRoom sr " +
            "LEFT JOIN FETCH s.screeningSeats ss " +
            "LEFT JOIN FETCH ss.seat seat " +
            "WHERE s.id = :id")
    Optional<Screening> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT s FROM Screening s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screeningRoom sr " +
            "ORDER BY s.screeningTime DESC")
    List<Screening> findAllWithMovieAndRoom();

    /**
     * 사용자용: 특정 날짜의 모든 상영 스케줄을 조회합니다. (시간 오름차순 정렬)
     * 상영 시작 후 10분까지는 예매 가능합니다.
     * @param date 조회할 날짜
     * @param currentDateTimeMinus10Min 현재 시간 - 10분 (이 시간 이후 시작 상영만 조회)
     * @return 해당 날짜의 모든 상영 스케줄 목록 (예매 가능한 상영만)
     */
    @Query("SELECT DISTINCT s FROM Screening s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screeningRoom sr " +
            "LEFT JOIN FETCH s.screeningSeats ss " +
            "WHERE s.screeningDate = :date AND s.screeningTime > :currentDateTimeMinus10Min " +
            "ORDER BY s.screeningTime ASC")
    List<Screening> findByScreeningDateForUser(@Param("date") LocalDate date, @Param("currentDateTimeMinus10Min") java.time.LocalDateTime currentDateTimeMinus10Min);

    /**
     * 사용자용: 특정 영화의 특정 날짜 상영 스케줄을 조회합니다. (시간 오름차순 정렬)
     * 상영 시작 후 10분까지는 예매 가능합니다.
     * @param movieId 영화 ID
     * @param date 조회할 날짜
     * @param currentDateTimeMinus10Min 현재 시간 - 10분 (이 시간 이후 시작 상영만 조회)
     * @return 해당 영화, 해당 날짜의 상영 스케줄 목록 (예매 가능한 상영만)
     */
    @Query("SELECT DISTINCT s FROM Screening s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screeningRoom sr " +
            "LEFT JOIN FETCH s.screeningSeats ss " +
            "WHERE m.id = :movieId AND s.screeningDate = :date AND s.screeningTime > :currentDateTimeMinus10Min " +
            "ORDER BY s.screeningTime ASC")
    List<Screening> findByMovieIdAndScreeningDateForUser(@Param("movieId") Long movieId, @Param("date") LocalDate date, @Param("currentDateTimeMinus10Min") java.time.LocalDateTime currentDateTimeMinus10Min);

    /**
     * 스케줄러용: 지정된 시간 이전에 시작한 상영들을 조회합니다.
     * @param dateTime 기준 시간
     * @return 해당 시간 이전에 시작한 상영 목록
     */
    @Query("SELECT s FROM Screening s WHERE s.screeningTime < :dateTime")
    List<Screening> findScreeningsStartedBefore(@Param("dateTime") java.time.LocalDateTime dateTime);
}