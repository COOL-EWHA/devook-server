package com.ewha.devookserver.domain.post;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EachPostResponseDto {

  private final Long id;
  private final String title;
  private final String thumbnail;
  private final String description;
  private final List<String> tags;
  private final String url;
  private final String createdAt;
  private final String memo;


  @Builder
  public EachPostResponseDto(Long id, String title, String thumbnail, String description, List<String> tags, String url, String createdAt, String memo){
    this.id=id;
    this.title=title;
    this.thumbnail=thumbnail;
    this.description=description;
    this.tags=tags;
    this.url=url;
    this.createdAt=createdAt;
    this.memo=memo;
  }
}
