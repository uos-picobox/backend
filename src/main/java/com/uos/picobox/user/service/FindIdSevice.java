package com.uos.picobox.user.service;

import com.uos.picobox.user.repository.CustomerRepository;
import com.uos.picobox.user.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindIdSevice {
    private final CustomerRepository customerRepository;
    private final GuestRepository guestRepository;

    public Long findCustomerIdByLoginId(String loginId) {
        return customerRepository.findIdByLoginId(loginId);
    }

    public Long findGuestIdByEmail(String email) {
        return guestRepository.findIdByEmail(email);
    }
}
