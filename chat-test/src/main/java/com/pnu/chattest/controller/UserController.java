package com.pnu.chattest.controller;

import com.pnu.chattest.dto.user.CreateUserDto;
import com.pnu.chattest.dto.user.UserInfoDto;
import com.pnu.chattest.service.UserService;
import com.pnu.chattest.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Iterable<UserInfoDto>> getAllUsers() {
        var users = userService.findAll();
        return ResponseEntity.ok(users.stream()
                .map(EntityMapper::toDto)
                .toList());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDto> getUserById(
            @PathVariable UUID userId
    ) {
        var user = userService.findById(userId);
        return ResponseEntity.ok(EntityMapper.toDto(user));
    }

    @PostMapping
    public ResponseEntity<UserInfoDto> createUser(
            @RequestBody CreateUserDto dto
    ) {
        var res = EntityMapper.toDto(userService.create(dto.nickname(), dto.chatRoomId()));
        return ResponseEntity.status(201).body(res);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId
    ) {
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}
