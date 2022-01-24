package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.QueryService;
import com.ewha.devookserver.service.TagService;
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
  private final TagService tagService;



  // 지금 q 때문에 문제 발생중

  /*
  @GetMapping("/test/dsl")
  public ResponseEntity<?> get_dsl
      (@RequestParam(name = "tags", required = false) String tags,
          @RequestParam(name = "cursor", required = false) Long cursor,
          @RequestParam(name="q", required = false)String question,
          @RequestHeader(value = "Authorization") String tokenGet
      ){

    System.out.println(tags);

    List<String> requiredTagList=new ArrayList<>();

    if (tags != null) {
      StringTokenizer tokens=new StringTokenizer(tags,",");

      while(tokens.hasMoreTokens()){
        requiredTagList.add(tokens.nextToken());
      }

      }


    System.out.println(requiredTagList);


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


      // 필터링에 해당하는 post_idx 의 배열 :: postTagList
      List<Long> postTagList=tagService.makePostTagList(requiredTagList);
      System.out.println(postTagList);

      // 11.21 @ 1:03:31 수정사항

      if(!postTagList.isEmpty()){
        if (cursor == null) {
          cursor = postRepository.findTopByUserIdxOrderByPostIdxDesc(userIdx).getPostIdx()
              + 1;//사용자의 가장 최근 글 값
        }
        // 여기 아래부터 시작
        return ResponseEntity.status(200).body(postService.responseListMaker
            (this.queryService.get(cursor, PageRequest.of(0, 10), userIdx, question,postTagList)));
      }




      if (cursor == null) {
        cursor = postRepository.findTopByUserIdxOrderByPostIdxDesc(userIdx).getPostIdx()
            + 1;//사용자의 가장 최근 글 값
      }
      return ResponseEntity.status(200).body(postService.responseListMaker
          (this.queryService.get(cursor, PageRequest.of(0, (int) limit), userIdx, question))
      );
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(401).body("어떤 에러인지 확인"+e);
    }

  }

   */
  @GetMapping("/bookmarks")
  public ResponseEntity<?> get_dsl
      (@RequestParam(name = "tags", required = false) String tags,
          @RequestParam(name = "cursor", required = false) Long cursor,
          @RequestParam(name="q", required = false)String question,
          @RequestHeader(value = "Authorization") String tokenGet
      ){


    if(cursor==null) System.out.println("cursornull");
    if(tags==null) System.out.println("tagsnull");
    if(question==null) System.out.println("questionnull");



    List<String> requiredTagList=new ArrayList<>();

    if (tags != null) {
      StringTokenizer tokens=new StringTokenizer(tags,",");

      while(tokens.hasMoreTokens()){
        requiredTagList.add(tokens.nextToken());
      }

    }
    System.out.println(requiredTagList);
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

      // 필터링에 해당하는 post_idx 의 배열 :: postTagList
      List<Long> postTagList=tagService.makePostTagList(requiredTagList);
      System.out.println(postTagList);

      // 11.21 @ 1:03:31 수정사항

      if (cursor == null) {
        try{
          cursor = postRepository.findTopByUserIdxOrderByPostIdxDesc(userIdx).getPostIdx()
              + 1;//사용자의 가장 최근 글 값
        }catch (Exception e){
          cursor= Long.valueOf(1);
        }

      }




      if(tags==null){
          // 여기 아래부터 시작
          return ResponseEntity.status(200).body(postService.responseListMaker
              (this.queryService.get(cursor, PageRequest.of(0, 10), userIdx, question)));
        }

        // 여기 아래부터 시작
        return ResponseEntity.status(200).body(postService.responseListMaker
            (this.queryService.get(cursor, PageRequest.of(0, 10), userIdx, question,postTagList)));


    } catch (Exception e) {
      e.printStackTrace();
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
