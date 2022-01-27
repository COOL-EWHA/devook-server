package com.ewha.devookserver.dto.post;

import java.util.List;
import lombok.Builder;
import lombok.Getter;


@Getter
public class PostListDto {


  private final Long id;
  private final String title;
  private final String thumbnail;
  private final String description;
  private final List<String> tags;
  private final String url;


  @Builder
  public PostListDto(Long id, String title, String thumbnail, String description, List<String> tags, String url){
    this.id=id;
    this.title=title;
    this.thumbnail=thumbnail;
    this.description=description;
    this.tags=tags;
    this.url=url;
  }
}
