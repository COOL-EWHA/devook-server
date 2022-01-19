package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class QueryController {

  private final UserService userService;
  private final PostService postService;
  private final OauthService oauthService;
  private final QueryRepository queryRepository;

  @GetMapping("/test/dsl")
  public List<Post> get_dsl(){
    return queryRepository.findAllPostByIdx();
  }

  @GetMapping("/test/dsl/tag")
  public List<PostTag> get_dslTag(){
    return queryRepository.findAllTagsByPost(57);
  }


  @GetMapping("/test/bookmarkList")
  public void getBoardsapi(@RequestParam(name="tags")String tags,
      @RequestParam(name="cursor")Long cursor,
      @RequestParam(name="limit")Integer limit) {


    List<String> tokens = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(tags, ",");
    while (tokenizer.hasMoreElements()) {
      tokens.add(tokenizer.nextToken());
    }


  }

}
