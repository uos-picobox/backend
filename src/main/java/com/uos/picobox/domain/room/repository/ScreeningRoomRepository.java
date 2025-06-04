package com.uos.picobox.domain.room.repository;

import com.uos.picobox.domain.room.entity.ScreeningRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ScreeningRoomRepository extends JpaRepository<ScreeningRoom, Long> {
    Optional<ScreeningRoom> findByRoomName(String roomName);
}