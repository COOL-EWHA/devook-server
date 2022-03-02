package com.ewha.devookserver.crawler;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestListDto {

  public String url;
  public String title;
  public String description;
  public String category;
  public String image;
}
