package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.RefrenceDto;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
public class RecommendService {

  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final UserBookmarkRepository userBookmarkRepository;

  WebClient postClient = WebClient.create(
      "https://chrome.devook.com/random/list");
  WebClient postClientPost = WebClient.create(
      "https://chrome.devook.com/random/post");
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
/*
  @GetMapping("/random/post")
  public ResponseEntity<?> randomReturn(@RequestHeader(value = "Authorization") Long postIndex) {
 */
public List<Post> getRandomPost(Long postIndex)
    throws JsonProcessingException {

  List<Post> newArray = new ArrayList<>();
  JsonNode result = postClient.get()
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .header("Authorization", String.valueOf(postIndex))
      .retrieve()
      .bodyToMono(String.class).map(s -> {
        ObjectMapper mapper = new ObjectMapper();
        try {
          return mapper.readTree(s);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }

        return null;
      })
      .block();

  ObjectMapper objectMapper = new ObjectMapper();


  if (result != null) {
    String returnValue = objectMapper.writeValueAsString(result);

    List<Post> returnPost = objectMapper.readValue(returnValue, new TypeReference<>() {
    });

    return returnPost;
  }




  return null;
}


  public List<Post> getRandom(String userIdx)
      throws JsonProcessingException {

    List<Post> newArray = new ArrayList<>();
    JsonNode result = postClient.get()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .header("Authorization", userIdx)
        .retrieve()
        .bodyToMono(String.class).map(s -> {
          ObjectMapper mapper = new ObjectMapper();
          try {
            return mapper.readTree(s);
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }

          return null;
        })
        .block();

    ObjectMapper objectMapper = new ObjectMapper();


    if (result != null) {
      String returnValue = objectMapper.writeValueAsString(result);

      List<Post> returnPost = objectMapper.readValue(returnValue, new TypeReference<>() {
      });

      return returnPost;
    }




    return null;
  }



}
