package com.uos.picobox.domain.point.service;

import com.uos.picobox.domain.point.dto.MyPointHistoryResponseDto;
import com.uos.picobox.domain.point.entity.PointHistory;
import com.uos.picobox.domain.point.repository.PointHistoryRepository;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {
    private final PointHistoryRepository pointHistoryRepository;
    private final CustomerRepository customerRepository;

    public MyPointHistoryResponseDto findPointHistory(Long customerId) {
        List<PointHistory> pointHistoryList = pointHistoryRepository.findAllByCustomerId(customerId);
        List<MyPointHistoryResponseDto.MyPointHistory> pointHistories = pointHistoryList.stream()
                .map(pointHistory -> MyPointHistoryResponseDto.MyPointHistory.builder()
                        .changeType(pointHistory.getChangeType())
                        .amount(pointHistory.getAmount())
                        .relatedReservationId(pointHistory.getRelatedReservationId())
                        .createdAt(pointHistory.getCreatedAt())
                        .build())
                .toList();

        MyPointHistoryResponseDto.MyPointHistory[] histories = pointHistories.toArray(new MyPointHistoryResponseDto.MyPointHistory[0]);

        Optional<Customer> option = customerRepository.findById(customerId);
        if (option.isEmpty()) {
            throw new NoSuchElementException("해당 session에 해당하는 회원 정보가 존재하지 않습니다.");
        }
        Customer customer = option.get();
        return new MyPointHistoryResponseDto(
                customer.getId(),
                customer.getLoginId(),
                customer.getPoints(),
                histories);
    }
}
