package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomIdOrderBySentAtAsc(String roomId);
}