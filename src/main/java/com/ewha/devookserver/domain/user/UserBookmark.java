package com.ewha.devookserver.domain.user;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "userBookmark")
public class UserBookmark {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userBookmarkIdx;
  private Long user_userIdx;
  private Long post_postIdx;
  @CreationTimestamp
  private Timestamp createdAt;

  @Builder
  public UserBookmark(Long userBookmarkIdx, Long user_userIdx, Long post_postIdx,
      Timestamp createdAt) {
    this.userBookmarkIdx = userBookmarkIdx;
    this.user_userIdx = user_userIdx;
    this.post_postIdx = post_postIdx;
    this.createdAt = createdAt;
  }

}
