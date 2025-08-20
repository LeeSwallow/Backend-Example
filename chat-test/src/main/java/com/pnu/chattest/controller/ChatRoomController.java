package com.pnu.chattest.controller;

import com.pnu.chattest.dto.chatroom.ChatRoomInfoDto;
import com.pnu.chattest.dto.chatroom.CreateChatRoomDto;
import com.pnu.chattest.service.ChatRoomService;
import com.pnu.chattest.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @GetMapping
    public ResponseEntity<List<ChatRoomInfoDto>> getChatRooms() {
        return ResponseEntity.ok(chatRoomService.findAll()
                .stream()
                .map(EntityMapper::toDto)
                .toList()
        );
    }

    @GetMapping("/{chatroomId}")
    public ResponseEntity<ChatRoomInfoDto> getChatRoomById(
            @PathVariable UUID chatroomId
    ) {
        return ResponseEntity.ok(EntityMapper.toDto(chatRoomService.findById(chatroomId)));
    }

    @PostMapping
    public ResponseEntity<ChatRoomInfoDto> createChatRoom(
            @RequestBody CreateChatRoomDto dto
    ) {
        var createdChatRoom = chatRoomService.create(EntityMapper.toEntity(dto));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityMapper.toDto(createdChatRoom));
    }

    @DeleteMapping("/{chatroomId}")
    public ResponseEntity<Void> deleteChatRoom(
            @PathVariable UUID chatroomId
    ) {
        chatRoomService.deleteById(chatroomId);
        return ResponseEntity.noContent().build();
    }
}
