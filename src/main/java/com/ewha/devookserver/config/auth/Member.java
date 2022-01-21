package com.ewha.devookserver.config.auth;

import com.ewha.devookserver.domain.user.Role;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String oauthId;
    private String name;
    private String email;
    private String imageUrl;
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private Role role;

    protected Member() {
    }

    @Builder
    public Member(Long id, String oauthId, String name, String email, String imageUrl, Role role, String refreshToken) {
        this.id = id;
        this.oauthId = oauthId;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.role = role;
        this.refreshToken=refreshToken;
    }

    public Member update(String name, String email, String imageUrl, String refreshToken) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.refreshToken=refreshToken;
        return this;
    }

    public Member updateRefreshToken(String refreshToken){
        this.refreshToken=refreshToken;
        return this;
    }
}
