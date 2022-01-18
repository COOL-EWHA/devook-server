package com.ewha.devookserver.domain.post;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.beans.ConstructorProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class PostLambdaDto {

  private final String title;
  private final String description;
  private final String image;

  @ConstructorProperties({"title", "description", "image"})
  public PostLambdaDto(String title, String description, String image){
    this.title=title;
    this.description=description;
    this.image=image;
  }
}
