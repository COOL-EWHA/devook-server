package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.EachPostResponseDto;
import com.ewha.devookserver.domain.post.EachPostResponseDtoForList;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
      @RequestParam(name = "postId", required = false) Long postId,
      @RequestParam(name = "bookmarkId", required = false) Long bookmarkId,
      @RequestParam(name = "cursor", required = false) Long cursor,
      @RequestParam(name = "limit", required = false) Long limit,
      @RequestParam(name = "q", required = false) String question,
      @RequestParam(name = "tags", required = false) String tags,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    List<String> requiredTagList = new ArrayList<>();

    if (tags != null) {
      StringTokenizer tokens = new StringTokenizer(tags, ",");

      while (tokens.hasMoreTokens()) {
        requiredTagList.add(tokens.nextToken());
      }

    }
    if (limit == null) {
      limit = Long.valueOf(10);
    }

    if (bookmarkId == null) {
      bookmarkId = postId;
    }
    if (postId == null) {
      postId = bookmarkId;
    }
    if (cursor == null) {
      try {
        cursor = Long.valueOf(100000);
      } catch (Exception e) {
        cursor = Long.valueOf(100000);
      }
    }

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return ResponseEntity.status(401).body("1");
    }
    System.out.println(oauthService.isUserExist(accessToken));
    if (!oauthService.isUserExist(accessToken)) {
      return ResponseEntity.status(401).body("2");
    }    // 유저 예외처리 완료
    String userIdx = oauthService.getUserIdx(accessToken);

    //유저 추천글 전체 목록 GET (분기)
    if (bookmarkId==null&&postId==null) {
      System.out.println("태그로 들어가서 검색!");

      List<Long> postTagList = tagService.makePostTagList(requiredTagList);

      return ResponseEntity.status(200).body(postService.responseListMaker
          (this.queryService.get(cursor, PageRequest.of(0, 10), userIdx, question, postTagList,
              true, requiredTagList, limit.intValue())));
    }

    System.out.println("태그말고 원래대로~");

if(tags==null){

    if (!recommendService.isPostByUser(postId, userIdx)) {
      return ResponseEntity.status(401).body("해당유저글이 아닐 때 오류 401오류리턴(메모삭제하기)");

  }

  Post post = postRepository.getPostByPostIdx(postId);
  List<PostTag> postTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());
  // 해당 글이 가진 태그 리스트 검색

  List<RefrenceDto> refrenceDtos = recommendService.calculateReference(postTagList);

  // 이제 각 post 당 refrence 를 알았으니, 이거 역순으로 정렬해서 리턴해주면 된다.

  return ResponseEntity.status(200).body(
      postService.responseListMaker(
          this.queryService.get(cursor, PageRequest.of(0, 10), refrenceDtos, limit.intValue(),
              userIdx)));
}
    return ResponseEntity.status(401).body("오류");

  }



  /*

  // 유저 추천글 목록 전체 GET
  @GetMapping("/posts")
  public ResponseEntity<?> userList
      (@RequestParam(name = "tags", required = false) String tags,
          @RequestParam(name = "cursor", required = false) Long cursor,
          @RequestParam(name="limit",required = false)Long limit,
          @RequestHeader(value = "Authorization") String tokenGet
      ){
    if(limit==null){
      limit= Long.valueOf(10);
    }

    if(cursor==null) System.out.println("cursornull");
    if(tags==null) System.out.println("tagsnull");

    List<String> requiredTagList=new ArrayList<>();

    if (tags != null) {
      StringTokenizer tokens=new StringTokenizer(tags,",");

      while(tokens.hasMoreTokens()){
        requiredTagList.add(tokens.nextToken());
      }

    }
    System.out.println(requiredTagList);

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
          cursor = Long.valueOf(100000);
        }catch (Exception e){
          cursor= Long.valueOf(100000);
        }
      }


      if(tags==null){
        // 여기 아래부터 시작
        return ResponseEntity.status(200).body(postService.responseListMaker
            (this.queryService.get(cursor, PageRequest.of(0, 10), userIdx, limit)));
      }

      // 여기 아래부터 시작
      return ResponseEntity.status(200).body(postService.responseListMaker
          (this.queryService.get(cursor, PageRequest.of(0, 10), userIdx, limit)));


    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(401).body("어떤 에러인지 확인"+e);
    }

  }







 List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());
      if (postIdxList.contains(post.getPostIdx()) && !post.getUserIdx().equals(userIdx)) {
       // filteredPostList.add(post);
        for(PostTag postTag:eachPostTagList){
          for(String string:requiredTagList){
            if(postTag.getPostTagName().equals(string)){
              count++;
            }
          }
        }

        RefrenceDto refrenceDto=new RefrenceDto();
        refrenceDto.setPost(post);
        refrenceDto.setRefrence(count);
        resultArray.add(refrenceDto);


      }
    }
    Collections.sort(resultArray);




   */


  @GetMapping("/posts/tags")
  public ResponseEntity<?> getTagList(
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    try {

      String accessToken = accessTokenGet.split(" ")[1];
      if (!oauthService.validatieTokenInput(accessToken)) {
        return ResponseEntity.status(401).body(" ");
      }
      System.out.println(oauthService.isUserExist(accessToken));
      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(401).body(" ");
      }    // 유저 예외처리 완료
      String userIdx = oauthService.getUserIdx(accessToken);
      System.out.println(userIdx);
      // 1. userIdx와 일치하는 post
      List<String> finalResponseString = postService.getPostTagList(userIdx);

      // 여기 String 배열 반환
      return ResponseEntity.status(200).body(finalResponseString);

    } catch (Exception e) {
      System.out.println(e);
      return ResponseEntity.status(401).body(" ");
    }
  }

  @GetMapping("/posts/{postId}")
  public ResponseEntity<?> eachPostResponse(
      @PathVariable(name = "postId")int bookmarkId,
      @RequestHeader(name="Authorization")String accessTokenGet){

    try {
      String accessToken = accessTokenGet.split(" ")[1];
      if (!oauthService.validatieTokenInput(accessToken)) {
        return ResponseEntity.status(401).body(" ");
      }
      System.out.println(oauthService.isUserExist(accessToken));
      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(401).body(" ");
      }    // 유저 예외처리 완료
      String userIdx = oauthService.getUserIdx(accessToken);

      if(postRepository.existsByPostIdx((long)bookmarkId))
      {
          Post userPost = postRepository.getPostByPostIdx((long)bookmarkId);


          List<String> tagList = postService.getEachPostTagList(bookmarkId);
          if(tagList.size()==0){
            tagList.add("태그1");
            tagList.add("태그2");
          }

          SimpleDateFormat format1 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
          Date convertedTime= userPost.getCreatedAt();
          String dBconvertedTime=format1.format(convertedTime);

          EachPostResponseDtoForList eachPostResponseDto=EachPostResponseDtoForList.builder()
              .id(userPost.getId())
              .title(userPost.getPostTitle())
              .thumbnail(userPost.getPostThumbnail())
              .description(userPost.getPostDescription())
              .tags(tagList)
              .url(userPost.getPostUrl())
              .build();

          return ResponseEntity.status(200).body(eachPostResponseDto);
        }
        return ResponseEntity.status(404).body("");
      } catch (Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.status(404).body("");
    }
  }
}