package com.ewha.devookserver.domain.user;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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

  @CreationTimestamp
  private Timestamp createdAt;

  @Enumerated(EnumType.STRING)
  private Role role;

  protected Member() {
  }

  @Builder
  public Member(Long id, String oauthId, String name, String email, String imageUrl, Role role,
      String refreshToken) {
    this.id = id;
    this.oauthId = oauthId;
    this.name = name;
    this.email = email;
    this.imageUrl = imageUrl;
    this.role = role;
    this.refreshToken = refreshToken;
  }

  public Member update(String name, String email, String imageUrl, String refreshToken) {
    this.name = name;
    this.email = email;
    this.imageUrl = imageUrl;
    this.refreshToken = refreshToken;
    return this;
  }

  public Member updateRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }
}
