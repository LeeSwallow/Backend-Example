package com.pnu.chattest.repository;

import com.pnu.chattest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByChatRoomId(UUID chatRoomId);
}
