package com.uos.picobox.user.service;

import com.uos.picobox.user.dto.MyPointsResponseDto;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPointsService {
    private final CustomerRepository customerRepository;

    public MyPointsResponseDto findPointsByCustomerId(Long customerId) {
        Optional<Customer> option = customerRepository.findById(customerId);
        if (option.isEmpty()) {
            throw new NoSuchElementException("해당 session에 해당하는 회원 정보가 존재하지 않습니다.");
        }
        Customer customer = option.get();
        return new MyPointsResponseDto(customer.getId(), customer.getLoginId(), customer.getPoints());
    }
}
