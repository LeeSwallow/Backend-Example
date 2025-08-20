package com.pnu.chattest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class ChatMessage extends BaseEntity {
    String content;

    @ManyToOne(fetch = FetchType.EAGER)
    User user;

    @ManyToOne
    ChatRoom chatRoom;

    public ChatMessage(String content, User user, ChatRoom chatRoom) {
        this.content = content;
        this.user = user;
        this.chatRoom = chatRoom;
    }
}