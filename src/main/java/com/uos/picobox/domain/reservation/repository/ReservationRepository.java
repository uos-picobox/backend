package com.uos.picobox.domain.reservation.repository;

import com.uos.picobox.domain.reservation.entity.Reservation;
import com.uos.picobox.global.enumClass.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomerIdOrderByReservationDateDesc(Long customerId);
    List<Reservation> findByGuestIdOrderByReservationDateDesc(Long guestId);
    boolean existsByScreeningId(Long screeningId);

    /**
     * 고객의 예매 내역을 상영 정보와 함께 조회 (완료된 예매만)
     * 상영 시간 기준으로 최신순 정렬
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.customer.id = :customerId " +
           "AND r.reservationStatus = :status " +
           "ORDER BY r.id DESC")
    List<Reservation> findByCustomerIdAndReservationStatusOrderByIdDesc(@Param("customerId") Long customerId,
                                                                    @Param("status") ReservationStatus status);

    /**
     * 게스트의 예매 내역을 상영 정보와 함께 조회 (완료된 예매만)
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.guest.id = :guestId " +
           "AND r.reservationStatus = :status " +
           "ORDER BY r.id DESC")
    List<Reservation> findByGuestIdAndReservationStatusOrderByIdDesc(@Param("guestId") Long guestId,
                                                                 @Param("status") ReservationStatus status);

    /**
     * 고객의 예매 내역을 ReservationStatus로 조회
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.customer.id = :customerId " +
           "AND r.reservationStatus IN :statuses " +
           "ORDER BY r.id DESC")
    List<Reservation> findByCustomerIdAndReservationStatusInOrderByIdDesc(@Param("customerId") Long customerId,
                                                                          @Param("statuses") List<ReservationStatus> statuses);

    /**
     * 게스트의 예매 내역을 ReservationStatus로 조회
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.guest.id = :guestId " +
           "AND r.reservationStatus IN :statuses " +
           "ORDER BY r.id DESC")
    List<Reservation> findByGuestIdAndReservationStatusInOrderByIdDesc(@Param("guestId") Long guestId,
                                                                       @Param("statuses") List<ReservationStatus> statuses);

    /**
     * 예매 상세 조회 (상영 정보, 결제 정보 포함) - 회원용
     */
    @Query("SELECT r FROM Reservation r " +
           "LEFT JOIN FETCH r.tickets t " +
           "LEFT JOIN FETCH r.payment p " +
           "WHERE r.id = :reservationId AND r.customer.id = :customerId")
    Optional<Reservation> findByIdAndCustomerIdWithDetails(@Param("reservationId") Long reservationId,
                                                          @Param("customerId") Long customerId);

    /**
     * 예매 상세 조회 (상영 정보, 결제 정보 포함) - 게스트용
     */
    @Query("SELECT r FROM Reservation r " +
           "LEFT JOIN FETCH r.tickets t " +
           "LEFT JOIN FETCH r.payment p " +
           "WHERE r.id = :reservationId AND r.guest.id = :guestId")
    Optional<Reservation> findByIdAndGuestIdWithDetails(@Param("reservationId") Long reservationId,
                                                        @Param("guestId") Long guestId);

    /**
     * 특정 영화의 총 예매 관객 수 조회
     */
    @Query("SELECT COUNT(t) FROM Reservation r " +
           "JOIN r.tickets t " +
           "JOIN Screening s ON r.screeningId = s.id " +
           "WHERE s.movie.id = :movieId " +
           "AND r.reservationStatus = 'COMPLETED'")
    Long countReservedAudienceByMovieId(@Param("movieId") Long movieId);

    /**
     * 전체 영화의 총 예매 관객 수 조회
     */
    @Query("SELECT COUNT(t) FROM Reservation r " +
           "JOIN r.tickets t " +
           "WHERE r.reservationStatus = 'COMPLETED'")
    Long countTotalReservedAudience();

    /**
     * 회원이 예매하고 COMPLETED 상태의 예매 ID 전체 조회
     */
    @Query("SELECT r.id FROM Reservation r WHERE r.reservationStatus = 'COMPLETED' AND r.customer IS NOT NULL")
    List<Long> findCompletedReservationIds();

}