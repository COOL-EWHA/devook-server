package com.ewha.devookserver.domain.post;

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
import org.hibernate.annotations.DynamicInsert;

@Setter
@Getter
@NoArgsConstructor
@DynamicInsert
@Entity
@Table(name = "userBookmark")
public class UserBookmark {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userBookmarkIdx;
  private Long user_userIdx;
  private Long post_postIdx;
  private String memo;

  @CreationTimestamp
  private Timestamp createdAt;

  private Boolean isRead;

  @Builder
  public UserBookmark(Long userBookmarkIdx, Long user_userIdx, Long post_postIdx, String memo,
      Timestamp createdAt) {
    this.userBookmarkIdx = userBookmarkIdx;
    this.user_userIdx = user_userIdx;
    this.post_postIdx = post_postIdx;
    this.memo = memo;
    this.createdAt = createdAt;
  }

}