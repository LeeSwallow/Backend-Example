package com.pnu.chattest.util;

import com.pnu.chattest.dto.chatroom.ChatRoomInfoDto;
import com.pnu.chattest.dto.chatroom.CreateChatRoomDto;
import com.pnu.chattest.dto.chatting.ChatMessageDto;
import com.pnu.chattest.dto.user.CreateUserDto;
import com.pnu.chattest.dto.user.UserInfoDto;
import com.pnu.chattest.entity.ChatMessage;
import com.pnu.chattest.entity.ChatRoom;
import com.pnu.chattest.entity.User;

public class EntityMapper {

    private EntityMapper() {
        // Prevent instantiation
    }

    public static UserInfoDto toDto(User user) {
        return new UserInfoDto(
                user.getId(),
                user.getNickname(),
                user.getLastActiveTime()
        );
    }

    public static ChatRoomInfoDto toDto(ChatRoom chatRoom) {
        return new ChatRoomInfoDto(
                chatRoom.getId(),
                chatRoom.getName(),
                chatRoom.getDescription(),
                chatRoom.getCreatedAt()
        );
    }

    public static ChatRoom toEntity(CreateChatRoomDto dto) {
        return new ChatRoom(
                dto.name(),
                dto.description()
        );
    }

    public static ChatMessageDto toDto(ChatMessage chatMessage) {
        return new ChatMessageDto(
                chatMessage.getId(),
                chatMessage.getUser().getId(),
                chatMessage.getUser().getNickname(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }

    public static ChatMessage toEntity(ChatMessageDto dto) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(dto.content());
        return chatMessage;
    }
}
