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
public class Post {

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
}
