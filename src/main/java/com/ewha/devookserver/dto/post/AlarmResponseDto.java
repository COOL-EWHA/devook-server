package com.ewha.devookserver.dto.post;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AlarmResponseDto implements Comparable<AlarmResponseDto> {

  public Long id;
  public String type;
  public String message;
  public String createdAt;
  public Boolean isRead;
  public Long bookmarkId;

  @Builder
  public AlarmResponseDto(Long id, String type, String message, String createdAt, Boolean isRead,
      Long bookmarkId) {
    this.id = id;
    this.type=type;
    this.message = message;
    this.createdAt = createdAt;
    this.isRead = isRead;
    this.bookmarkId = bookmarkId;
  }

  @Override
  public int compareTo(AlarmResponseDto o) {
    if (this.getId() > o.getId()) {
      return -1;
    } else if (this.getId() < o.getId()) {
      return 1;
    } else {
      return 0;
    }
  }
}
