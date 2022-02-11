package com.ewha.devookserver.domain.post;

import static javax.persistence.GenerationType.IDENTITY;

import java.sql.Timestamp;
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
@Table(name = "alarm")
public class Alarm implements Comparable<Alarm> {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long alarmIdx;

  private Long userIdx;
  private String message;
  private Boolean isRead;
  private Long postIdx;
  private String type;

  @CreationTimestamp
  private Timestamp createdAt;

  @Builder
  public Alarm(Long userIdx, String message, Boolean isRead, Long postIdx, String type) {
    this.userIdx = userIdx;
    this.message = message;
    this.isRead = isRead;
    this.postIdx = postIdx;
    this.type = type;
  }

  @Override
  public int compareTo(Alarm o) {
    if (this.getPostIdx() > o.getPostIdx()) {
      return -1;
    } else if (this.getPostIdx() < o.getPostIdx()) {
      return 1;
    } else {
      return 0;
    }
  }
}
