package com.ewha.devookserver.domain.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RefrenceDto implements Comparable<RefrenceDto> {

  // TODO : reference DTO algorithm 수정

  // 태그 A에 해당하는 글 개수 5개, 태그 B에 해당하는 글 개수 3개, 태그 C에 해당하는 글 개수 2개라면
  //글 10개씩 보여줄 때 태그 A에 해당하는 글 5개, 태그 B에 해당하는 글 개수 3개, 태그 C에 해당하는 글 개수 2개 보여주기

  public Post post;
  public int refrence;

  public RefrenceDto(Post post, int refrence) {
    this.post = post;
    this.refrence = refrence;
  }

  @Override
  public int compareTo(RefrenceDto o) {
    if (this.getPost().getPostIdx() > o.getPost().getPostIdx()) {
      return -1;
    } else if (this.getRefrence() < o.getRefrence()) {
      return 1;
    } else {
      return 0;
    }
  }
}
