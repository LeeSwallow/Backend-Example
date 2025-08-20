package com.pnu.chattest.service;

import com.pnu.chattest.entity.ChatRoom;
import com.pnu.chattest.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom create(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAll();
    }

    public ChatRoom findById(UUID chatroomId) {
        return chatRoomRepository.findById(chatroomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 채팅방이 존재하지 않습니다." + chatroomId));
    }

    public void deleteById(UUID chatroomId) {
        chatRoomRepository.deleteById(chatroomId);
    }

    ChatRoom getReferenceById(UUID chatroomId) {
        return chatRoomRepository.getReferenceById(chatroomId);
    }
}
