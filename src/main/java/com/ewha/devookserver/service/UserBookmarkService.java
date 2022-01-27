package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.user.UserBookmark;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserBookmarkService {
  private final UserBookmarkRepository userBookmarkRepository;
  private final PostRepository postRepository;


  // 사용자의 idx 값 넣으면, 그 사용자가 직접 등록한 'post' table에 중복되지 않는 userBookmark table 에 존재하는 post 글들을 리턴해주고 싶다.
/*
  public List<Post> bookmarkExcludeUserPost(Long userIdx){

    List<Long> postUserBookmarkList=new ArrayList<>();
    List<Post> finalResultList=new ArrayList<>();

    List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(userIdx);

    for(UserBookmark userBookmark:userBookmarkList){
      postUserBookmarkList.add(userBookmark.getPost_postIdx());
    }

    // 이제 사용자의 post table에서 검색해보고, 제외하기

    List<Post> postList=postRepository.findAllByUserIdx(String.valueOf(userIdx));

    System.out.println("user가 직접 등록한 post");
    for(Post post:postList){
      System.out.println(post.getPostIdx()+"번");
    }

    for(Post post:postList){
      if(!postUserBookmarkList.contains(post.getPostIdx())){
        finalResultList.add(post);
      }
    }


    System.out.println("최종리턴");
    for(Post post:finalResultList){
      System.out.println(post.getPostIdx()+"번");
    }


    return finalResultList;
  }

 */
  public List<Post> bookmarkExcludeUserPosts(Long userIdx){

    List<Post> postUserBookmarkList=new ArrayList<>();
    List<Post> finalResultList=new ArrayList<>();

    List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(userIdx);


    // 북마크로 등록한 글들의 post를 가져온다.
    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      post.setCreatedAt(userBookmark.getCreatedAt());
      postUserBookmarkList.add(post);
    }

    // 이제 사용자의 post table에서 검색해보고, 제외하기

    List<Post> postList=postRepository.findAllByUserIdx(String.valueOf(userIdx));

    System.out.println("user가 직접 등록한 post");
    for(Post post:postList){
      System.out.println(post.getPostIdx()+"번");
    }



    for(Post post:postList){
      finalResultList.add(post);
    }

    // 이 아래 문이 안먹는다.
    for(Post post:postUserBookmarkList){
      if(finalResultList.contains(post)){
        System.out.println(post.getPostIdx()+post.getPostTitle());
        finalResultList.add(post);
      }
    }


    int count=0;

    for(Post post:postUserBookmarkList){
      count=0;
      for(Post posts:finalResultList){

        if(post.getPostIdx()==posts.getPostIdx()){
          count++;
        }

      }
      if(count==0){
        finalResultList.add(post);
      }
    }



    System.out.println("최종리턴");
    for(Post post:finalResultList){
      System.out.println(post.getPostIdx()+"번");
    }


    return finalResultList;
  }






}
