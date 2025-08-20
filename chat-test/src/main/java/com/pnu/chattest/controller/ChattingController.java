package com.pnu.chattest.controller;

import com.pnu.chattest.dto.chatting.Action;
import com.pnu.chattest.dto.chatting.ChatMessageDto;
import com.pnu.chattest.dto.chatting.UserActionDto;
import com.pnu.chattest.dto.user.UserInfoDto;
import com.pnu.chattest.entity.ChatMessage;
import com.pnu.chattest.entity.User;
import com.pnu.chattest.service.ChattingService;
import com.pnu.chattest.service.UserService;
import com.pnu.chattest.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@MessageMapping("/chats")
@RequiredArgsConstructor
public class ChattingController {

    private final ChattingService chattingService;
    private final UserService userService;


    /**
     * 채팅방 메시지 구독 이벤트
     *
     * 처음 메시지가 구독되면 채팅방 메시지 목록을 반환한다.
     * 사용자는 접속 시간과 관계없이 전체 메시지 목록을 받아볼 수 있다.
     */
    @SubscribeMapping("/{roomId}")
    public List<ChatMessageDto> onSubscribeChatRoom(
            @DestinationVariable UUID roomId
    ) {
        return chattingService.findByChatRoomId(roomId)
                .stream()
                .map(EntityMapper::toDto)
                .toList();
    }

    /**
     * 채팅방 멤버 구독 이벤트
     * 
     * 채팅방에 입장하면 모든 멤버를 받아온다.
     * 추후 enter, leave 이벤트가 발생하면 멤버 목록을 갱신한다.
     */
    @SubscribeMapping("/{roomId}/members")
    public List<UserInfoDto> onSubscribeMembersFeed(@DestinationVariable UUID roomId) {
        return chattingService.findUsersInChatRoom(roomId)
                .stream()
                .map(EntityMapper::toDto)
                .toList();
    }

    /**
     * 채팅방 입장 이벤트
     * 
     * 채팅방에 입장하면 멤버 목록을 갱신하기 위해 멤버 목록 구독 이벤트를 호출한다.
     * 구독자들은 해당 이벤트를 통해 입장된 유저의 정보를 받아올 수 있고 이를 client 단에서 관리한다.
     */
    @MessageMapping("/{roomId}/enter")
    @SendTo("/topic/chats/{roomId}/members")
    public UserActionDto enterChatRoom(@DestinationVariable UUID roomId, UserInfoDto userInfo) {
        User user = userService.updateLastActiveTime(userInfo.id());
        return new UserActionDto(
                EntityMapper.toDto(user),
                Action.ENTER
        );
    }

    /**
     * 채팅 메시지 전송 이벤트
     * 
     * 채팅 메시지를 전송하면 해당 메시지를 브로드캐스트한다.
     * 구독자들은 실시간으로 메시지를 받아볼 수 있다.
     */
    @MessageMapping("/{roomId}/send")
    @SendTo("/topic/chats/{roomId}")
    public ChatMessageDto sendMessage(@DestinationVariable UUID roomId, ChatMessageDto message) {
        ChatMessage chatMessage = chattingService.sendMessageTo(roomId, message.senderId(), message.content());
        return EntityMapper.toDto(chatMessage);
    }

    /**
     * 채팅방 퇴장 이벤트
     * 
     * 채팅방에서 퇴장하면 멤버 목록을 갱신하기 위해 멤버 목록 구독 이벤트를 호출한다.
     * 구독자들은 해당 이벤트를 통해 퇴장된 유저의 정보를 받아올 수 있고 이를 client 단에서 관리한다.
     */
    @MessageMapping("/{roomId}/leave")
    @SendTo("/topic/chats/{roomId}/members")
    public UserActionDto leaveChatRoom(@DestinationVariable UUID roomId, UserInfoDto userInfo) {
        User user = userService.updateLastActiveTime(userInfo.id());
        userService.deleteById(user.getId());
        return new UserActionDto(
                EntityMapper.toDto(user),
                Action.LEAVE
        );
    }
}

