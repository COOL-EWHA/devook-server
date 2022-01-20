package com.ewha.devookserver.domain.post;

import static javax.persistence.GenerationType.IDENTITY;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import javax.persistence.*;

import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name= "post")
public class Post implements Comparable<Post>{

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long postIdx;

  private String postTitle;
  private String postThumbnail;
  private String postDescription;
  private String postUrl;
  private String postMemo;
  private String userIdx;

  @CreationTimestamp
  private Timestamp createdAt;

  @UpdateTimestamp
  private Timestamp updatedAt;

  @Builder
  public Post(String postTitle, String postThumbnail, String postDescription, String postUrl, String postMemo, String userIdx){
    this.postTitle=postTitle;
    this.postThumbnail=postThumbnail;
    this.postDescription=postDescription;
    this.postUrl=postUrl;
    this.postMemo=postMemo;
    this.userIdx=userIdx;
  }

  public Long getId() {
    return postIdx;
  }

  // post 객체 Collection으로 정렬할 때 사용할 함수.
  @Override
  public int compareTo(Post o) {

    if(this.getPostIdx()>o.getPostIdx()){
      return -1;
    }
    else if(this.getPostIdx()<o.getPostIdx()){
      return 1;
    }else{
      return 0;
    }
  }
}
