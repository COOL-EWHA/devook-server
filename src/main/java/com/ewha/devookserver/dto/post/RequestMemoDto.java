package com.ewha.devookserver.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestMemoDto {

  public Boolean isRead;
  public String memo;
  public String dueDate;

}
