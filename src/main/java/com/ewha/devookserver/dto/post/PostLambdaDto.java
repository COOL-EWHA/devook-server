package com.ewha.devookserver.dto.post;


import java.beans.ConstructorProperties;
import lombok.Getter;

@Getter
public class PostLambdaDto {

  private final String title;
  private final String description;
  private final String image;

  @ConstructorProperties({"title", "description", "image"})
  public PostLambdaDto(String title, String description, String image) {
    this.title = title;
    this.description = description;
    this.image = image;
  }
}
