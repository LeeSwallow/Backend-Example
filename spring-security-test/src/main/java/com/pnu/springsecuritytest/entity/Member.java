package com.pnu.springsecuritytest.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {
    private String email;
    private String password;
}
