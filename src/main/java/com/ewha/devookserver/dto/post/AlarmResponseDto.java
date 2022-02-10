package com.ewha.devookserver.dto.post;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AlarmResponseDto {

  public Long id;
  public String message;
  public String createdAt;
  public Boolean isRead;
  public Long bookmarkId;

  @Builder
  public AlarmResponseDto(Long id, String message, String createdAt, Boolean isRead,
      Long bookmarkId) {
    this.id = id;
    this.message = message;
    this.createdAt = createdAt;
    this.isRead = isRead;
    this.bookmarkId = bookmarkId;
  }

}
