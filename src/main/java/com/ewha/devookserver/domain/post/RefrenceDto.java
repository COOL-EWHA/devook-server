package com.ewha.devookserver.domain.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RefrenceDto implements Comparable<RefrenceDto> {

  public Post post;
  public int refrence;

  public RefrenceDto(Post post, int refrence) {
    this.post = post;
    this.refrence = refrence;
  }

  @Override
  public int compareTo(RefrenceDto o) {
    if (this.getRefrence() > o.getRefrence()||this.getPost().getPostIdx()>o.getPost().getPostIdx()) {
      return -1;
    } else if (this.getRefrence() < o.getRefrence()) {
      return 1;
    } else {
      return 0;
    }
  }
}
