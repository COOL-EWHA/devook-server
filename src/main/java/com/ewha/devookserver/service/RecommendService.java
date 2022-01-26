package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RecommendService {
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final UserBookmarkRepository userBookmarkRepository;


  // 해당 글이 bookmarked 된 글인지 Boolean 리턴

  public boolean checkIsBookmarked(Long postId, String userIdx){

    if(userBookmarkRepository.existsByPost_postIdxAndUser_userIdx(postId, Long.valueOf(userIdx))!=null){
      System.out.println("ok");
      return true;
    }
    System.out.println("존재 하지 않음." + postId+"" +userIdx);
    return false;
  }


  public boolean isPostByUser(Long postId, String userIdx){
    if(!postRepository.existsByPostIdx(postId)){
      System.out.println("존재안함!!");
      return false;
    }
    if(postRepository.getPostByPostIdx(postId).getUserIdx().equals(userIdx)){
      System.out.println("존재!!");

      return true;
    }

    return false;
  }
  public List<RefrenceDto> calculateReference(List<PostTag> postTagList){

    List<Post> allPost=postRepository.findAll();
    List<RefrenceDto> resultArray=new ArrayList<>();
    int count;

    for(Post post:allPost){
      count=0;
      List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());

      for(PostTag postTag:eachPostTagList){
        for(PostTag postTag1:postTagList){
          if(postTag.getPostTagName().equals(postTag1.getPostTagName())){
            count++;
          }
        }
      }
      RefrenceDto refrenceDto=new RefrenceDto();
      refrenceDto.setPost(post);
      refrenceDto.setRefrence(count);

      resultArray.add(refrenceDto);
    }
    return resultArray;
  }


  //이제 reference 순으로 배열을 정리하고, cursor에 따라 리턴해주면 됨.


}
