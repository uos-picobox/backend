package com.uos.picobox.domain.reservation.repository;

import com.uos.picobox.global.enumClass.PaymentStatus;
import com.uos.picobox.domain.reservation.entity.Reservation;
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
           "AND r.paymentStatus = :status " +
           "ORDER BY r.id DESC")
    List<Reservation> findByCustomerIdAndPaymentStatusOrderByIdDesc(@Param("customerId") Long customerId,
                                                                    @Param("status") PaymentStatus status);

    /**
     * 게스트의 예매 내역을 상영 정보와 함께 조회 (완료된 예매만)
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.guest.id = :guestId " +
           "AND r.paymentStatus = :status " +
           "ORDER BY r.id DESC")
    List<Reservation> findByGuestIdAndPaymentStatusOrderByIdDesc(@Param("guestId") Long guestId,
                                                                 @Param("status") PaymentStatus status);

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
}