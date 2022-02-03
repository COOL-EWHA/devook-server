package com.ewha.devookserver.domain.post;


import static javax.persistence.GenerationType.IDENTITY;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
@Table(name = "notification")
public class Notification {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long notificationId;

  private Long postIdx;
  private Long bookmarkIdx;
  private Long userIdx;
  private Boolean isPost;

  @CreationTimestamp
  private Timestamp createdAt;
  private Date dueDate;

  private LocalDateTime alertTime;

  @Builder
  public Notification
      (Long postIdx, Long bookmarkIdx, Long userIdx, Boolean isPost, Date dueDate,
          LocalDateTime alertTime) {
    this.postIdx = postIdx;
    this.bookmarkIdx = bookmarkIdx;
    this.userIdx = userIdx;
    this.isPost = isPost;
    this.dueDate = dueDate;
    this.alertTime = alertTime;
  }

}
