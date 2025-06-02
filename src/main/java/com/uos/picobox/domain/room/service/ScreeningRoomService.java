package com.uos.picobox.domain.room.service;

import com.uos.picobox.domain.room.dto.RowDefinitionDto;
import com.uos.picobox.domain.room.dto.ScreeningRoomRequestDto;
import com.uos.picobox.domain.room.dto.ScreeningRoomResponseDto;
import com.uos.picobox.domain.room.entity.ScreeningRoom;
import com.uos.picobox.domain.room.entity.Seat;
import com.uos.picobox.domain.room.repository.ScreeningRoomRepository;
import com.uos.picobox.domain.room.repository.SeatRepository;
import com.uos.picobox.domain.screening.repository.ScreeningRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScreeningRoomService {

    private final ScreeningRoomRepository screeningRoomRepository;
    private final SeatRepository seatRepository;
    private final ScreeningRepository screeningRepository;

    @Transactional
    public ScreeningRoomResponseDto registerScreeningRoom(ScreeningRoomRequestDto requestDto) {
        if (requestDto.getRowDefinitions() == null || requestDto.getRowDefinitions().isEmpty()) {
            throw new IllegalArgumentException("상영관 생성 시 좌석 배치 정보는 필수입니다.");
        }

        screeningRoomRepository.findByRoomName(requestDto.getRoomName())
                .ifPresent(sr -> {
                    throw new IllegalArgumentException("이미 존재하는 상영관 이름입니다: " + requestDto.getRoomName());
                });

        int calculatedCapacity = requestDto.getRowDefinitions().stream()
                .mapToInt(RowDefinitionDto::getNumberOfSeats)
                .sum();

        ScreeningRoom screeningRoom = ScreeningRoom.builder()
                .roomName(requestDto.getRoomName())
                .capacity(calculatedCapacity)
                .build();

        List<Seat> seats = generateSeatsForRoom(screeningRoom, requestDto.getRowDefinitions());
        screeningRoom.setSeats(seats);

        ScreeningRoom savedScreeningRoom = screeningRoomRepository.save(screeningRoom);
        return new ScreeningRoomResponseDto(savedScreeningRoom);
    }

    private List<Seat> generateSeatsForRoom(ScreeningRoom room, List<RowDefinitionDto> rowDefinitions) {
        List<Seat> seats = new ArrayList<>();
        for (RowDefinitionDto rowDef : rowDefinitions) {
            if (rowDef.getNumberOfSeats() == null || rowDef.getNumberOfSeats() < 1) {
                throw new IllegalArgumentException("각 행의 좌석 수는 1 이상이어야 합니다: " + rowDef.getRowIdentifier());
            }
            for (int i = 1; i <= rowDef.getNumberOfSeats(); i++) {
                seats.add(Seat.builder()
                        .seatNumber(rowDef.getRowIdentifier() + i)
                        .build());
            }
        }
        return seats;
    }

    @Transactional
    public ScreeningRoomResponseDto editScreeningRoom(Long roomId, ScreeningRoomRequestDto requestDto) {
        ScreeningRoom screeningRoom = screeningRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상영관을 찾을 수 없습니다: " + roomId));

        if (requestDto.getRoomName() != null && !screeningRoom.getRoomName().equals(requestDto.getRoomName())) {
            screeningRoomRepository.findByRoomName(requestDto.getRoomName())
                    .filter(sr -> !sr.getId().equals(roomId))
                    .ifPresent(sr -> {
                        throw new IllegalArgumentException("이미 존재하는 상영관 이름입니다: " + requestDto.getRoomName());
                    });
            screeningRoom.updateRoomName(requestDto.getRoomName());
        }

        if (requestDto.getRowDefinitions() != null) {
            if (requestDto.getRowDefinitions().isEmpty()) {
                throw new IllegalArgumentException("좌석 정보가 없습니다. (상영관 이름만 수정할 row 컬럼을 아예 지우세요)");
            }

            log.info("상영관 ID {}의 좌석 배치 변경 요청. 기존 스케줄 확인 필요.", roomId);
            boolean hasScreenings = screeningRepository.existsByScreeningRoomId(roomId);
            if (hasScreenings) {
                throw new IllegalStateException("이미 상영 스케줄이 잡힌 상영관의 좌석 배치는 변경할 수 없습니다. (Room ID: " + roomId + ")");
            }

            log.warn("상영관 ID {}의 좌석 배치가 변경됩니다. 기존 좌석은 삭제되고 새로 생성됩니다.", roomId);

            seatRepository.deleteAllByScreeningRoomId(roomId);
            screeningRoom.clearSeats();

            int calculatedCapacity = requestDto.getRowDefinitions().stream()
                    .mapToInt(RowDefinitionDto::getNumberOfSeats)
                    .sum();
            screeningRoom.updateCapacity(calculatedCapacity);

            List<Seat> newSeats = generateSeatsForRoom(screeningRoom, requestDto.getRowDefinitions());
            screeningRoom.setSeats(newSeats);
        }
        return new ScreeningRoomResponseDto(screeningRoom);
    }

    public List<ScreeningRoomResponseDto> findAllScreeningRooms() {
        return screeningRoomRepository.findAll().stream()
                .map(ScreeningRoomResponseDto::new)
                .collect(Collectors.toList());
    }

    public ScreeningRoomResponseDto findScreeningRoomById(Long roomId) {
        ScreeningRoom screeningRoom = screeningRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상영관을 찾을 수 없습니다: " + roomId));
        return new ScreeningRoomResponseDto(screeningRoom);
    }

    @Transactional
    public void removeScreeningRoom(Long roomId) {
        ScreeningRoom screeningRoom = screeningRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상영관을 찾을 수 없습니다: " + roomId));

        log.info("상영관 ID {} 삭제 요청. 기존 스케줄 확인 필요.", roomId);
        boolean hasScreenings = screeningRepository.existsByScreeningRoomId(roomId);
        if (hasScreenings) {
            throw new IllegalStateException("해당 상영관에 상영 스케줄이 존재하여 삭제할 수 없습니다. (Room ID: " + roomId + ")");
        }
        log.warn("상영관 ID {} 및 연관된 모든 좌석이 삭제됩니다.", roomId);
        screeningRoomRepository.delete(screeningRoom);
    }
}