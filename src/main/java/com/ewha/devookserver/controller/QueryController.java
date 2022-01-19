package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.QueryService;
import com.ewha.devookserver.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
  private final QueryService queryService;
  private final PostRepository postRepository;


  @GetMapping("/test/dsl")
  public ResponseEntity<?> get_dsl
      (@RequestParam(name = "tags", required = false) String tags,
          @RequestParam(name = "cursor", required = false) Long cursor,
          @RequestParam(name="q", required = false)String question,
          @RequestHeader(value = "Authorization") String tokenGet
      ){


    if(question==""){
      System.out.println("빈공백");
    }
    if(question==" "){
      System.out.println("한칸띄고");
    }
    System.out.println(question);

    System.out.println(tags);

    int limit = 10;

    try {
      String accessToken = tokenGet.split(" ")[1];

      // 로그인 안 한 유저
      if (accessToken == "undefined") {
        return ResponseEntity.status(401).body("");
      }

      // 존재하지 않는 유저
      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(404).body("");
      }

      String userIdx = oauthService.getUserIdx(accessToken);
      if (cursor == null) {
        cursor = postRepository.findTopByUserIdxOrderByPostIdxDesc(userIdx).getPostIdx()
            + 1;//사용자의 가장 최근 글 값
      }
      return ResponseEntity.status(200).body(postService.responseListMaker
          (this.queryService.get(cursor, PageRequest.of(0, (int) limit), userIdx, question))
      );
    } catch (Exception e) {
      return ResponseEntity.status(401).body("어떤 에러인지 확인"+e);
    }

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
