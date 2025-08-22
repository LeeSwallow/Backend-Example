package com.pnu.springsecuritytest.controller;

import com.pnu.springsecuritytest.common.mapper.DtoMapper;
import com.pnu.springsecuritytest.common.mapper.EntityMapper;
import com.pnu.springsecuritytest.dto.user.UserChangePasswordRequest;
import com.pnu.springsecuritytest.dto.user.UserCreateRequest;
import com.pnu.springsecuritytest.dto.user.UserEditRequest;
import com.pnu.springsecuritytest.dto.user.UserResponse;
import com.pnu.springsecuritytest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody UserCreateRequest dto
    ) {
        return new ResponseEntity<>(
                EntityMapper.toDto(userService.create(DtoMapper.toEntity(dto))),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll()
                .stream()
                .map(EntityMapper::toDto)
                .toList());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(EntityMapper.toDto(userService.getById(id)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(EntityMapper.toDto(userService.getById(currentUserId)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> editUser(
            @PathVariable UUID id,
            @RequestBody UserEditRequest dto
    ) {
        return ResponseEntity.ok(EntityMapper.toDto(userService.edit(id, DtoMapper.toConsumer(dto))));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserChangePasswordRequest dto
    ) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        userService.changePassword(
                currentUserId,
                dto.oldPassword(),
                dto.newPassword(),
                dto.confirmNewPassword()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id
    ) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        userService.delete(currentUserId);
        return ResponseEntity.noContent().build();
    }
}
