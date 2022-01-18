package com.ewha.devookserver.config.auth;

import com.ewha.devookserver.domain.user.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserProfile {
    private final String oauthId;
    private final String email;
    private final String name;
    private final String imageUrl;
    private final String accessToken;
    private final String refreshToken;

    @Builder
    public UserProfile(String oauthId, String email, String name, String imageUrl, String accessToken, String refreshToken) {
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
        this.accessToken=accessToken;
        this.refreshToken=refreshToken;
    }

    public Member toMember(String refreshToken) {
        return Member.builder()
                .oauthId(oauthId)
                .email(email)
                .name(name)
                .imageUrl(imageUrl)
                .role(Role.GUEST)
                .refreshToken(refreshToken)
                .build();
    }
}
