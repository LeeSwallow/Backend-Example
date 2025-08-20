package com.pnu.chattest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@RequiredArgsConstructor
public class User extends BaseEntity {

    String nickname;
    LocalDateTime lastActiveTime;

    @ManyToOne
    ChatRoom chatRoom;

    public User(String nickname, ChatRoom chatRoom) {
        this.nickname = nickname;
        this.lastActiveTime = LocalDateTime.now();
        this.chatRoom = chatRoom;
    }
}
