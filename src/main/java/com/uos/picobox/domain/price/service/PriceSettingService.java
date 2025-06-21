package com.uos.picobox.domain.price.service;

import com.uos.picobox.domain.price.dto.PriceSettingRequestDto;
import com.uos.picobox.domain.price.dto.PriceSettingResponseDto;
import com.uos.picobox.domain.price.entity.RoomTicketTypePrice;
import com.uos.picobox.domain.price.entity.RoomTicketTypePrice.RoomTicketTypePriceId;
import com.uos.picobox.domain.price.repository.RoomTicketTypePriceRepository;
import com.uos.picobox.domain.room.entity.ScreeningRoom;
import com.uos.picobox.domain.room.repository.ScreeningRoomRepository;
import com.uos.picobox.domain.ticket.entity.TicketType;
import com.uos.picobox.domain.ticket.repository.TicketTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceSettingService {

    private final RoomTicketTypePriceRepository priceRepository;
    private final ScreeningRoomRepository screeningRoomRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    public PriceSettingResponseDto setOrUpdatePrice(PriceSettingRequestDto requestDto) {
        ScreeningRoom screeningRoom = screeningRoomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("상영관을 찾을 수 없습니다: ID " + requestDto.getRoomId()));
        TicketType ticketType = ticketTypeRepository.findById(requestDto.getTicketTypeId())
                .orElseThrow(() -> new EntityNotFoundException("티켓 종류를 찾을 수 없습니다: ID " + requestDto.getTicketTypeId()));

        RoomTicketTypePriceId id = new RoomTicketTypePriceId(screeningRoom.getId(), ticketType.getId());
        Optional<RoomTicketTypePrice> existingPriceOpt = priceRepository.findById(id);

        RoomTicketTypePrice priceSetting;
        if (existingPriceOpt.isPresent()) {
            priceSetting = existingPriceOpt.get();
            priceSetting.updatePrice(requestDto.getPrice());
            log.info("가격 정보가 업데이트되었습니다: 상영관 '{}', 티켓 '{}', 가격 {}", screeningRoom.getRoomName(), ticketType.getTypeName(), requestDto.getPrice());
        } else {
            priceSetting = RoomTicketTypePrice.builder()
                    .screeningRoom(screeningRoom)
                    .ticketType(ticketType)
                    .price(requestDto.getPrice())
                    .build();
            log.info("새로운 가격 정보가 설정되었습니다: 상영관 '{}', 티켓 '{}', 가격 {}", screeningRoom.getRoomName(), ticketType.getTypeName(), requestDto.getPrice());
        }
        RoomTicketTypePrice savedPriceSetting = priceRepository.save(priceSetting);
        return new PriceSettingResponseDto(savedPriceSetting);
    }

    public PriceSettingResponseDto getPrice(Long roomId, Long ticketTypeId) {
        RoomTicketTypePriceId id = new RoomTicketTypePriceId(roomId, ticketTypeId);
        RoomTicketTypePrice priceSetting = priceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("상영관(ID:%d)의 티켓종류(ID:%d)에 대한 가격 정보를 찾을 수 없습니다.", roomId, ticketTypeId)
                ));
        return new PriceSettingResponseDto(priceSetting);
    }

    public List<PriceSettingResponseDto> getPricesForRoom(Long roomId) {
        if (!screeningRoomRepository.existsById(roomId)) {
            throw new EntityNotFoundException("상영관을 찾을 수 없습니다: ID " + roomId);
        }
        List<RoomTicketTypePrice> prices = priceRepository.findByScreeningRoomIdWithDetails(roomId);
        return prices.stream()
                .map(PriceSettingResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removePrice(Long roomId, Long ticketTypeId) {
        RoomTicketTypePriceId id = new RoomTicketTypePriceId(roomId, ticketTypeId);
        if (!priceRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    String.format("상영관(ID:%d)의 티켓종류(ID:%d)에 대한 가격 정보를 찾을 수 없습니다.", roomId, ticketTypeId)
            );
        }
        priceRepository.deleteById(id);
        log.info("가격 정보가 삭제되었습니다: 상영관 ID {}, 티켓 종류 ID {}", roomId, ticketTypeId);
    }
}