package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.QueryService;
import com.ewha.devookserver.service.RecommendService;
import com.ewha.devookserver.service.RefrenceDto;
import com.ewha.devookserver.service.TagService;
import com.ewha.devookserver.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class RecommendController {

  private final UserService userService;
  private final PostService postService;
  private final OauthService oauthService;
  private final RecommendService recommendService;
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final QueryRepository queryRepository;
  private final QueryService queryService;
  private final TagService tagService;


  @GetMapping("/posts")
  public ResponseEntity<?> bookMarkLists(
      @RequestParam(name = "postId", required = false)Long postId,
      @RequestParam(name="cursor", required = false)Long cursor,
      @RequestParam(name="limit",required = false)Long limit,
      @RequestHeader(name="Authorization")String accessTokenGet){

    limit=Long.valueOf(10);

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return ResponseEntity.status(401).body(" ");
    }
    System.out.println(oauthService.isUserExist(accessToken));
    if (!oauthService.isUserExist(accessToken)) {
      return ResponseEntity.status(401).body(" ");
    }    // 유저 예외처리 완료
    String userIdx = oauthService.getUserIdx(accessToken);


    if(!recommendService.isPostByUser(postId, userIdx)){
      return ResponseEntity.status(401).body("해당유저글이 아닐 때 오류 401오류리턴(메모삭제하기)");
    }

    Post post = postRepository.getPostByPostIdx(postId);
    List<PostTag> postTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());
    // 해당 글이 가진 태그 리스트 검색

    if (cursor == null) {
      try{
        cursor = Long.valueOf(1000);
      }catch (Exception e){
        cursor= Long.valueOf(1000);
      }
    }



    List<RefrenceDto> refrenceDtos=recommendService.calculateReference(postTagList);

    // 이제 각 post 당 refrence 를 알았으니, 이거 역순으로 정렬해서 리턴해주면 된다.


    return ResponseEntity.status(200).body(
        postService.responseListMaker(this.queryService.get(cursor, PageRequest.of(0,10), refrenceDtos))
    );
  }



}
