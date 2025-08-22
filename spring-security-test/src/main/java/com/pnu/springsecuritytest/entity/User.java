package com.pnu.springsecuritytest.entity;

import com.pnu.springsecuritytest.entity.type.ProviderType;
import com.pnu.springsecuritytest.entity.type.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {
    @Column(unique = true, nullable = false)

    private String email;

    private String password;

    private Integer age;

    private String name;

    private Boolean enabled;

    private Boolean accountNonExpired;

    private Boolean credentialsNonExpired;

    private Boolean accountNonLocked;

    private Integer  failedLoginAttempts;

    private LocalDateTime lastLoginAt;

    @ManyToMany
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER
    )
    private List<AuthProvider> authProviders = new ArrayList<>();

    public Set<RoleType> getRoleTypes() {
        return roles.stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    public Set<ProviderType> getProviderTypes() {
        return authProviders.stream()
            .map(auth -> auth.getProvider().getName())
            .collect(Collectors.toSet());
    }
}
