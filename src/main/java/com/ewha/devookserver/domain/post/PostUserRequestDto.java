package com.ewha.devookserver.domain.post;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostUserRequestDto {
  public String postId;
  public String url;
  public String memo;
}
