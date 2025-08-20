package com.pnu.chattest.dto.chatting;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageDto(
    UUID id,
    UUID senderId,
    String nickname,
    String content,
    LocalDateTime sentAt
) {

}
