package com.uos.picobox.user.scheduler;

import com.uos.picobox.user.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GuestScheduler {
    private final GuestRepository guestRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void deleteExpiredGuests() {
        LocalDateTime threshold = LocalDateTime.now();
        guestRepository.deleteExpiredGuests(threshold);
    }
}
