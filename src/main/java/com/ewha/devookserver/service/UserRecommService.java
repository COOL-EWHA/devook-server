package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.UserBookmark;
import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.ewha.devookserver.repository.UserRecommRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserRecommService {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final UserRecommRepository userRecommRepository;
  private final UserBookmarkRepository userBookmarkRepository;
  private final TagRepository tagRepository;


  // userTagList TODO
  public List<String> getPostUserTagList(String userIdx) {
    List<Post> returnPost = postRepository.findAllByUserIdx(userIdx);

    List<UserBookmark> addBookmark = userBookmarkRepository.findAllByUser_userIdx(
        Long.valueOf(userIdx));

    for (UserBookmark userBookmark : addBookmark) {
      returnPost.add(postRepository.getPostByPostIdx(userBookmark.getPostIdx())
      );
    }

    List<String> searchResponseDtoList = new ArrayList<>();

    for (Post post : returnPost) {
      List<PostTag> postTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());

      for (PostTag postTag : postTagList) {
        if (!searchResponseDtoList.contains(postTag.getPostTagName())) {
          searchResponseDtoList.add(postTag.getPostTagName());
        }
      }
    }

    return searchResponseDtoList;
  }

  public List<String> getPostTagList(String userIdx) {
    List<Post> returnPost = postRepository.findAllByUserIdx(userIdx);

    List<String> searchResponseDtoList = new ArrayList<>();

    for (Post post : returnPost) {
      List<PostTag> postTagList = userRecommRepository.findAllTagsByPost(
          post.getPostIdx().intValue());

      for (PostTag postTag : postTagList) {
        if (!searchResponseDtoList.contains(postTag.getPostTagName())) {
          searchResponseDtoList.add(postTag.getPostTagName());
        }
      }
    }

    return searchResponseDtoList;

  }

  public List<String> getAllTagList() {
    List<Post> returnPost = postRepository.findAll();

    List<String> searchResponseDtoList = new ArrayList<>();

    for (Post post : returnPost) {
      List<PostTag> postTagList = userRecommRepository.findAllTagsByPost(
          post.getPostIdx().intValue());

      for (PostTag postTag : postTagList) {
        if (!searchResponseDtoList.contains(postTag.getPostTagName())) {
          searchResponseDtoList.add(postTag.getPostTagName());
        }
      }
    }

    return searchResponseDtoList;

  }
}
