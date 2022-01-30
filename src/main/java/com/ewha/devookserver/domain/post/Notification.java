package com.ewha.devookserver.domain.post;


import static javax.persistence.GenerationType.IDENTITY;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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

  private Long post_postIdx;
  private Long user_userIdx;
  private boolean isPost;
  private boolean isRead;

  @CreationTimestamp
  private Timestamp createdAt;
  private LocalDateTime dueDate;

  private Date alertTime;

}
