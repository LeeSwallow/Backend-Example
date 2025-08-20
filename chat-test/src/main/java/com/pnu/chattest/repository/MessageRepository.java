package com.pnu.chattest.repository;

import com.pnu.chattest.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findAllByChatRoomId(UUID chatRoomId);
}
