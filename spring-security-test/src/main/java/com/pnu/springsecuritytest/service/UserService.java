package com.pnu.springsecuritytest.service;

import com.pnu.springsecuritytest.entity.User;
import com.pnu.springsecuritytest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다: " + id));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다: " + email));
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public User edit(UUID userId, Consumer<User> consumer) {
        User existingUser = getById(userId);

        consumer.accept(existingUser);

        return userRepository.save(existingUser);
    }

    public void changePassword(UUID userId, String oldPassword, String newPassword, String confirmPassword) {
        User user = getById(userId);

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void delete(UUID id) {
        getById(id); // 존재 여부 확인
        userRepository.deleteById(id);
    }
}
