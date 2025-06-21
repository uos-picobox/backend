package com.uos.picobox.domain.screening.service;

import com.uos.picobox.domain.movie.entity.Movie;
import com.uos.picobox.domain.movie.repository.MovieRepository;
import com.uos.picobox.domain.room.entity.ScreeningRoom;
import com.uos.picobox.domain.room.entity.Seat;
import com.uos.picobox.domain.room.repository.ScreeningRoomRepository;
import com.uos.picobox.domain.screening.dto.*;
import com.uos.picobox.domain.screening.entity.Screening;
import com.uos.picobox.domain.screening.entity.ScreeningSeat;
import com.uos.picobox.domain.screening.entity.SeatStatus;
import com.uos.picobox.domain.screening.repository.ScreeningRepository;
import com.uos.picobox.domain.screening.repository.ScreeningSeatRepository;
import com.uos.picobox.domain.reservation.repository.ReservationRepository;
import com.uos.picobox.domain.price.entity.RoomTicketTypePrice;
import com.uos.picobox.domain.price.repository.RoomTicketTypePriceRepository;
import com.uos.picobox.domain.ticket.entity.TicketType;
import com.uos.picobox.domain.ticket.repository.TicketTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final ScreeningRoomRepository screeningRoomRepository;
    private final ScreeningSeatRepository screeningSeatRepository;
    private final ReservationRepository reservationRepository;
    private final RoomTicketTypePriceRepository roomTicketTypePriceRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    public ScreeningResponseDto registerScreening(ScreeningRequestDto requestDto) {
        Movie movie = movieRepository.findById(requestDto.getMovieId())
                .orElseThrow(() -> new EntityNotFoundException("영화를 찾을 수 없습니다: ID " + requestDto.getMovieId()));
        ScreeningRoom screeningRoom = screeningRoomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("상영관을 찾을 수 없습니다: ID " + requestDto.getRoomId()));

        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            throw new IllegalArgumentException("영화의 상영 시간(duration) 정보가 유효하지 않습니다.");
        }

        LocalDateTime screeningStartTime = requestDto.getScreeningTime();
        LocalDate screeningDate = screeningStartTime.toLocalDate();
        LocalDateTime screeningEndTime = screeningStartTime.plusMinutes(movie.getDuration());

        validateNoOverlap(screeningRoom.getId(), screeningStartTime, screeningEndTime, null, screeningDate);

        int nextSequence = calculateNextScreeningSequence(screeningRoom.getId(), screeningDate, screeningStartTime);

        Screening screening = Screening.builder()
                .movie(movie)
                .screeningRoom(screeningRoom)
                .screeningTime(screeningStartTime)
                .screeningDate(screeningDate)
                .screeningSequence(nextSequence)
                .build();

        List<ScreeningSeat> screeningSeats = createScreeningSeatsForScreening(screening, screeningRoom.getSeats());
        screening.replaceScreeningSeats(screeningSeats);

        Screening savedScreening = screeningRepository.save(screening);
        log.info("새로운 상영 스케줄이 등록되었습니다: ID {}, 영화: {}, 상영관: {}, 시간: {}, 회차: {}",
                savedScreening.getId(), movie.getTitle(), screeningRoom.getRoomName(), screeningStartTime, nextSequence);
        return new ScreeningResponseDto(savedScreening);
    }

    private List<ScreeningSeat> createScreeningSeatsForScreening(Screening screening, List<Seat> roomSeats) {
        if (roomSeats == null || roomSeats.isEmpty()) {
            log.warn("상영관 ID {}에 좌석 정보가 없습니다. 상영 좌석을 생성할 수 없습니다.", screening.getScreeningRoom().getId());
            throw new IllegalStateException("상영관에 좌석이 설정되어 있지 않아 스케줄을 등록할 수 없습니다. (Room ID: " + screening.getScreeningRoom().getId() + ")");
        }
        return roomSeats.stream()
                .map(seat -> ScreeningSeat.builder()
                        .seat(seat)
                        .seatStatus(SeatStatus.AVAILABLE)
                        .build())
                .collect(Collectors.toList());
    }

    private int calculateNextScreeningSequence(Long roomId, LocalDate screeningDate, LocalDateTime newScreeningTime) {
        List<Screening> screeningsOnDate = screeningRepository.findByScreeningRoomIdAndScreeningDateOrderByScreeningTimeAsc(roomId, screeningDate);
        List<LocalDateTime> allTimes = new ArrayList<>();
        for (Screening s : screeningsOnDate) {
            allTimes.add(s.getScreeningTime());
        }
        allTimes.add(newScreeningTime);
        allTimes.sort(Comparator.naturalOrder());
        return allTimes.indexOf(newScreeningTime) + 1;
    }

    private void validateNoOverlap(Long roomId, LocalDateTime newStartTime, LocalDateTime newEndTime, Long screeningIdToExclude, LocalDate targetDate) {
        List<Screening> existingScreenings = screeningRepository.findByScreeningRoomIdAndScreeningDateOrderByScreeningTimeAsc(roomId, targetDate);
        for (Screening existing : existingScreenings) {
            if (existing.getId().equals(screeningIdToExclude)) {
                continue;
            }
            if (existing.getMovie() == null || existing.getMovie().getDuration() == null || existing.getMovie().getDuration() <= 0) {
                log.warn("기존 상영 스케줄 ID {}의 영화 또는 상영시간 정보가 유효하지 않아 정확한 겹침 확인이 어렵습니다.", existing.getId());
                continue;
            }
            LocalDateTime existingStartTime = existing.getScreeningTime();
            LocalDateTime existingEndTime = existingStartTime.plusMinutes(existing.getMovie().getDuration());

            if (newStartTime.isBefore(existingEndTime) && newEndTime.isAfter(existingStartTime)) {
                throw new IllegalArgumentException(
                        String.format("선택한 시간에 이미 다른 상영 스케줄이 존재합니다. (겹치는 스케줄: 영화 '%s', 시간: %s - %s)",
                                existing.getMovie().getTitle(),
                                existingStartTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                                existingEndTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                );
            }
        }
    }

    @Transactional
    public ScreeningResponseDto editScreening(Long screeningId, ScreeningRequestDto requestDto) {
        Screening screening = screeningRepository.findByIdWithDetails(screeningId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상영 스케줄을 찾을 수 없습니다: " + screeningId));

        // 실제 예매 확인 로직
        boolean hasReservations = reservationRepository.existsByScreeningId(screeningId);

        LocalDateTime newScreeningStartTime = requestDto.getScreeningTime();
        LocalDate newScreeningDate = newScreeningStartTime.toLocalDate();

        boolean timeChanged = !screening.getScreeningTime().equals(newScreeningStartTime);
        boolean movieChanged = !screening.getMovie().getId().equals(requestDto.getMovieId());
        boolean roomChanged = !screening.getScreeningRoom().getId().equals(requestDto.getRoomId());

        if (hasReservations && (timeChanged || movieChanged || roomChanged)) {
            throw new IllegalStateException("이미 예매 내역이 있는 상영 스케줄의 시간, 영화 또는 상영관은 변경할 수 없습니다. 모든 예매를 취소한 후 시도해주세요. (Screening ID: " + screening.getId() + ")");
        }

        Movie newMovie = movieRepository.findById(requestDto.getMovieId())
                .orElseThrow(() -> new EntityNotFoundException("영화를 찾을 수 없습니다: ID " + requestDto.getMovieId()));
        ScreeningRoom newScreeningRoom = screeningRoomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("상영관을 찾을 수 없습니다: ID " + requestDto.getRoomId()));

        if (newMovie.getDuration() == null || newMovie.getDuration() <= 0) {
            throw new IllegalArgumentException("새로 선택한 영화의 상영 시간 정보가 유효하지 않습니다.");
        }

        LocalDateTime newScreeningEndTime = newScreeningStartTime.plusMinutes(newMovie.getDuration());

        validateNoOverlap(newScreeningRoom.getId(), newScreeningStartTime, newScreeningEndTime, screeningId, newScreeningDate);

        screening.setMovie(newMovie);
        screening.setScreeningRoom(newScreeningRoom);
        screening.updateScreeningTimeAndDate(newScreeningStartTime);

        int newSequence = calculateNextScreeningSequenceForUpdate(newScreeningRoom.getId(), newScreeningDate, newScreeningStartTime, screeningId);
        screening.updateScreeningSequence(newSequence);

        if (!screening.getScreeningRoom().getId().equals(newScreeningRoom.getId()) || screening.getScreeningSeats().isEmpty()) {
            log.info("상영관 변경 또는 기존 좌석 정보 없음. 상영 ID {}의 좌석 정보를 재생성합니다.", screeningId);
            screeningSeatRepository.deleteAllByScreeningId(screening.getId());
            screening.clearScreeningSeats();
            List<ScreeningSeat> newScreeningSeats = createScreeningSeatsForScreening(screening, newScreeningRoom.getSeats());
            screening.replaceScreeningSeats(newScreeningSeats);
        }

        Screening updatedScreening = screeningRepository.save(screening);
        log.info("상영 스케줄이 수정되었습니다: ID {}", screeningId);
        return new ScreeningResponseDto(updatedScreening);
    }

    private int calculateNextScreeningSequenceForUpdate(Long roomId, LocalDate screeningDate, LocalDateTime newScreeningTime, Long excludeScreeningId) {
        List<Screening> screeningsOnDate = screeningRepository.findByScreeningRoomIdAndScreeningDateOrderByScreeningTimeAsc(roomId, screeningDate);
        List<LocalDateTime> allTimes = new ArrayList<>();
        for (Screening s : screeningsOnDate) {
            if (s.getId().equals(excludeScreeningId)) continue;
            allTimes.add(s.getScreeningTime());
        }
        allTimes.add(newScreeningTime);
        allTimes.sort(Comparator.naturalOrder());
        return allTimes.indexOf(newScreeningTime) + 1;
    }

    public List<ScreeningResponseDto> findAllScreenings() {
        return screeningRepository.findAllWithMovieAndRoom().stream()
                .map(ScreeningResponseDto::new)
                .collect(Collectors.toList());
    }

    public ScreeningResponseDto findScreeningById(Long screeningId) {
        Screening screening = screeningRepository.findByIdWithDetails(screeningId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상영 스케줄을 찾을 수 없습니다: " + screeningId));
        return new ScreeningResponseDto(screening);
    }

    @Transactional
    public void removeScreening(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상영 스케줄을 찾을 수 없습니다: " + screeningId));
        // 실제 예매 확인 로직
        boolean hasReservations = reservationRepository.existsByScreeningId(screeningId);
        if (hasReservations) {
            throw new IllegalStateException("이미 예매가 진행된 상영 스케줄은 삭제할 수 없습니다. (Screening ID: " + screeningId + ")");
        }
        screeningRepository.delete(screening);
        log.info("상영 스케줄이 삭제되었습니다: ID {}", screeningId);
    }

    // --- 사용자용 API 메소드 ---

    /**
     * 특정 날짜의 모든 상영 스케줄 목록을 조회합니다.
     * @param date 조회할 날짜
     * @return 해당 날짜의 상영 스케줄 목록
     */
    public List<ScreeningScheduleResponseDto> getScreeningSchedulesByDate(LocalDate date) {
        List<Screening> screenings = screeningRepository.findByScreeningDateForUser(date);
        return screenings.stream()
                .map(ScreeningScheduleResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 영화의 특정 날짜 상영 스케줄 목록을 조회합니다.
     * @param movieId 영화 ID
     * @param date 조회할 날짜
     * @return 해당 영화, 해당 날짜의 상영 스케줄 목록
     */
    public List<ScreeningScheduleResponseDto> getScreeningSchedulesByMovieAndDate(Long movieId, LocalDate date) {
        List<Screening> screenings = screeningRepository.findByMovieIdAndScreeningDateForUser(movieId, date);
        return screenings.stream()
                .map(ScreeningScheduleResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 상영의 전체 좌석 상태 목록을 조회합니다. (사용자용)
     * @param screeningId 상영 ID
     * @return 좌석 배치 및 상태 정보
     */
    public ScreeningSeatsResponseDto getSeatsForScreening(Long screeningId) {
        // findByIdWithDetails를 사용하면 상영 정보와 좌석 정보가 모두 fetch join 되어 쿼리 효율이 좋습니다.
        Screening screening = screeningRepository.findByIdWithDetails(screeningId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상영 스케줄을 찾을 수 없습니다: " + screeningId));

        List<ScreeningSeatStatusDto> seatDtos = screening.getScreeningSeats().stream()
                .map(ScreeningSeatStatusDto::fromEntity)
                // 좌석 번호 순으로 정렬 (예: A1, A2, B1, B2...)
                .sorted(Comparator.comparing(ScreeningSeatStatusDto::getSeatNumber))
                .collect(Collectors.toList());

        return ScreeningSeatsResponseDto.toDto(screening, seatDtos);
    }

    /**
     * 특정 상영의 티켓 유형별 가격 정보를 조회합니다.
     * @param screeningId 상영 ID
     * @return 티켓 유형별 가격 정보
     */
    public ScreeningTicketPricesResponseDto getTicketPricesForScreening(Long screeningId) {
        Screening screening = screeningRepository.findByIdWithDetails(screeningId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상영 스케줄을 찾을 수 없습니다: " + screeningId));

        // 해당 상영관의 모든 티켓 유형 가격 조회
        List<RoomTicketTypePrice> roomPrices = roomTicketTypePriceRepository.findByScreeningRoomIdWithDetails(screening.getScreeningRoom().getId());
        
        List<ScreeningTicketPricesResponseDto.TicketPriceInfo> ticketPrices = new ArrayList<>();
        
        for (RoomTicketTypePrice roomPrice : roomPrices) {
            ticketPrices.add(new ScreeningTicketPricesResponseDto.TicketPriceInfo(
                    roomPrice.getTicketType().getId(),
                    roomPrice.getTicketType().getTypeName(),
                    roomPrice.getPrice()
            ));
        }

        return new ScreeningTicketPricesResponseDto(
                screening.getId(),
                screening.getScreeningRoom().getId(),
                ticketPrices
        );
    }
}

