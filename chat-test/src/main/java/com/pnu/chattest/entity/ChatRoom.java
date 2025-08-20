package com.pnu.chattest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class ChatRoom extends BaseEntity{

    private String name;

    private String description;

    public ChatRoom(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
