package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.RefrenceDto;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

  public boolean checkIsBookmarked(Long postId, String userIdx) {

    return userBookmarkRepository.existsByPost_postIdxAndUser_userIdx(postId, Long.valueOf(userIdx))
        != null;
  }


  public boolean isPostByUser(Long postId, String userIdx) {
    if (!postRepository.existsByPostIdx(postId)) {
      return false;
    }
    return postRepository.getPostByPostIdx(postId).getUserIdx().equals(userIdx);
  }

  public List<RefrenceDto> calculateReference(List<PostTag> postTagList) {

    List<Post> allPost = postRepository.findAll();
    List<RefrenceDto> resultArray = new ArrayList<>();
    int count;

    for (Post post : allPost) {
      count = 0;
      List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
          post.getPostIdx().intValue());

      for (PostTag postTag : eachPostTagList) {
        for (PostTag postTag1 : postTagList) {
          if (postTag.getPostTagName().equals(postTag1.getPostTagName())) {
            count++;
          }
        }
      }
      RefrenceDto refrenceDto = new RefrenceDto();
      refrenceDto.setPost(post);
      refrenceDto.setRefrence(count);

      resultArray.add(refrenceDto);
    }
    Collections.sort(resultArray);
    return resultArray;
  }

  public List<RefrenceDto> calculateReferenceOfPost(List<PostTag> postTagList) {

    List<Post> allPost = postRepository.findAll();
    List<RefrenceDto> resultArray = new ArrayList<>();
    int count;

    for (Post post : allPost) {
      count = 0;
      List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
          post.getPostIdx().intValue());

      for (PostTag postTag : eachPostTagList) {
        for (PostTag postTag1 : postTagList) {
          if (postTag.getPostTagName().equals(postTag1.getPostTagName())) {
            count += 10;
          }
          if (post.getPostTitle().contains(postTag1.getPostTagName())) {
            count += 2;
          }
          if (post.getPostDescription().contains(postTag1.getPostTagName())) {
            count++;
          }
        }
      }
      RefrenceDto refrenceDto = new RefrenceDto();
      refrenceDto.setPost(post);
      refrenceDto.setRefrence(count);

      resultArray.add(refrenceDto);
      Collections.sort(resultArray);
    }
    return resultArray;
  }

}
