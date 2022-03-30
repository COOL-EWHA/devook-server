package com.ewha.devookserver.domain.post;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "postTag")
public class PostTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long postTagIdx;
  private String postTagName;
  private Integer post_postIdx;

  @Builder
  public PostTag(String postTagName, Integer post_postIdx) {
    this.postTagName = postTagName;
    this.post_postIdx = post_postIdx;
  }


}
