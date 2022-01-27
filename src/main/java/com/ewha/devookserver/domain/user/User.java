package com.ewha.devookserver.domain.user;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userIdx;
  private String userEmail;
  private String userNickname;
  private String providerType;
  private String userPicture;

  @CreationTimestamp
  private Timestamp createdAt;
  private Role userRole;


  @OneToOne
  @JoinColumn(name = "userToken_userTokenIdx")
  private UserToken userToken;


  @Builder
  public User(String userEmail, String userNickname, String providerType, Role userRole,
      String userPicture) {
    this.userEmail = userEmail;
    this.userNickname = userNickname;
    this.providerType = providerType;
    this.userRole = userRole;
    this.userPicture = userPicture;
  }

  public User update(String userNickname, String userPicture) {
    this.userNickname = userNickname;
    this.userPicture = userPicture;

    return this;
  }

  public User roleUpdate(String userRole) {
    this.userRole = Role.valueOf(userRole);
    return this;
  }

  public String getRoleKey() {
    return this.userRole.getKey();
  }
}
