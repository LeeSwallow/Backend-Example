package com.pnu.chattest.service;

import com.pnu.chattest.entity.User;
import com.pnu.chattest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;

    public User create(String nickname, UUID chatRoomId) {
        User user = new User(nickname, chatRoomService.findById(chatRoomId));
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByChatRoomId(UUID chatRoomId) {
        return userRepository.findAllByChatRoomId(chatRoomId);
    }

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다.: " + userId));
    }

    public User updateLastActiveTime(UUID userId) {
        User user = findById(userId);
        user.setLastActiveTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void deleteById(UUID userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }
}
