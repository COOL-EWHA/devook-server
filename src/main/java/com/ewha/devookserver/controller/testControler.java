package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.TestPost;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.repository.TestPostRepo;
import com.ewha.devookserver.repository.TestPostTagRepo;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class testControler {

  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final TestPostRepo testPostRepo;
  private final TestPostTagRepo testPostTagRepo;

  @GetMapping("/testAll")
  public void changeCreatedAt() throws InterruptedException {
    // 먼저 DB에 있는 것을 가져오기
    List<Post> shufflePost = postRepository.findAll();

    Collections.shuffle(shufflePost);

    for (Post post : shufflePost) {
      postRepository.save(post);
    }
  }

  @GetMapping("/test")
  public void get() {
    List<TestPost> findall = testPostRepo.findAll();
    Collections.shuffle(findall);
    for (TestPost testPost : findall) {

      String name = testPostTagRepo.getTestPostTagByPost_postIdx(testPost.getPostIdx().intValue())
          .getPostTagName();

      Post post = Post.builder()
          .postUrl(testPost.getPostUrl())
          .postTitle(testPost.getPostTitle())
          .postThumbnail(testPost.getPostThumbnail())
          .postDescription(testPost.getPostDescription())
          .userIdx("1")
          .build();
      postRepository.save(post);

      int postIdx = postRepository.getPostByPostUrlAndPostDescription(post.getPostUrl(), post.getPostDescription()).getPostIdx().intValue();

      PostTag postTag = new PostTag();
      postTag.setPost_postIdx(postIdx);
      postTag.setPostTagName(name);
      tagRepository.save(postTag);

      System.out.println(testPost.getPostIdx() + "   ~~ " + name);
    }
  }


}
