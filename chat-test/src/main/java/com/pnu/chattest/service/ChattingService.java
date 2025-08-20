package com.pnu.chattest.service;

import com.pnu.chattest.dto.chatting.Action;
import com.pnu.chattest.dto.chatting.UserActionDto;
import com.pnu.chattest.entity.ChatRoom;
import com.pnu.chattest.entity.ChatMessage;
import com.pnu.chattest.entity.User;
import com.pnu.chattest.repository.MessageRepository;
import com.pnu.chattest.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChattingService {

    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public List<User> findUsersInChatRoom(UUID chatroomId) {
        return userService.findByChatRoomId(chatroomId);
    }

    public List<ChatMessage> findByChatRoomId(UUID chatroomId) {
        return messageRepository.findAllByChatRoomId(chatroomId);
    }

    public User updateLastActiveTime(UUID userId) {
        return userService.updateLastActiveTime(userId);
    }

    public ChatMessage sendMessageTo(UUID chatroomId, UUID senderId, String content) {
        ChatRoom chatRoom = chatRoomService.getReferenceById(chatroomId);
        User sender = updateLastActiveTime(senderId);

        ChatMessage chatMessage = new ChatMessage(content, sender, chatRoom);
        chatMessage = messageRepository.save(chatMessage);

        UserActionDto userActionDto = new UserActionDto(
                EntityMapper.toDto(sender),
                Action.SEND
        );
        messagingTemplate.convertAndSend("/topic/chats/" + chatroomId, userActionDto);

        return chatMessage;
    }
}
