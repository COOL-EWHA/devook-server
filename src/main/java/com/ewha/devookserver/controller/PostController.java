package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.EachPostResponseDto;
import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostLabmdaRequestDto;
import com.ewha.devookserver.domain.post.PostLambdaDto;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.PostUserRequestDto;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.UserService;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
public class PostController {

  private final UserService userService;
  private final PostService postService;
  private final OauthService oauthService;
  private final PostRepository postRepository;

  @GetMapping("/bookmarks")
  public ResponseEntity<?> getBoardsapi(@RequestParam(name = "tags", required = false) String tags,
      @RequestParam(name = "cursor", required = false) Long cursor,
      @RequestParam(name="q", required = false)String question,
      @RequestHeader(value = "Authorization") String tokenGet
      ) {

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
            (this.postService.get(cursor, PageRequest.of(0, (int) limit), userIdx, question))
        );
      } catch (Exception e) {
        return ResponseEntity.status(404).body("계정오류");
      }
  }

  @PostMapping("/bookmarks")
  public ResponseEntity<?> testPost(
      @RequestHeader(value = "Authorization") String tokenGet,
      HttpServletResponse response, @RequestBody PostUserRequestDto postUserRequestDto) {

    System.out.println(postUserRequestDto.getMemo());

    try {
      System.out.println(tokenGet + "요청");
      String accessToken = tokenGet.split(" ")[1];
      if (Objects.equals(accessToken, "undefined")) {
        return ResponseEntity.status(401).body("");
      }

      // 존재하지 않는 유저
      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(401).body("");
      }
      String userIdx = oauthService.getUserIdx(accessToken);

      if (postService.isPostUserExists(postUserRequestDto.getUrl(), userIdx)) {
        System.out.println("이미 존재하는 글.");
        return ResponseEntity.status(201).body("");
      }

      PostLabmdaRequestDto postLabmdaRequestDto = new PostLabmdaRequestDto();
      postLabmdaRequestDto.setUrl(postUserRequestDto.getUrl());

      String url = postLabmdaRequestDto.getUrl();
      PostLambdaDto postLambdaDto = postService.getPostInfo(postLabmdaRequestDto);

      postService.savePost(
          postUserRequestDto.getMemo(),
          postUserRequestDto.getUrl(),
          postLambdaDto.getDescription(),
          postLambdaDto.getTitle(),
          postLambdaDto.getImage(), userIdx);

      return ResponseEntity.status(201).body("");
    } catch (Exception e) {
      System.out.println(e);
      return ResponseEntity.status(401).body("");
    }
  }


  @DeleteMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<?> deletePost(
      @PathVariable("bookmarkId") int bookmarkId,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];

    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    System.out.println(oauthService.isUserExist(accessToken));
    if (!oauthService.isUserExist(accessToken)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }    // 유저 예외처리 완료
    if (postService.deletePost(bookmarkId, oauthService.getUserIdx(accessToken))) {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return ResponseEntity.status(401).body(" ");
  }

  @GetMapping("/bookmarks/tags")
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

    }catch(Exception e){
      System.out.println(e);
      return ResponseEntity.status(401).body(" ");
    }

  }

  @GetMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<?> eachPostResponse(
      @PathVariable(name = "bookmarkId")int bookmarkId,
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
        if(postRepository.getPostByPostIdx((long)bookmarkId).getUserIdx().equals(userIdx)){
          Post userPost = postRepository.getPostByPostIdx((long)bookmarkId);


          List<String> tagList = postService.getEachPostTagList(bookmarkId);
          if(tagList.size()==0){
            tagList.add("태그1");
            tagList.add("태그2");
          }

          SimpleDateFormat format1 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
          Date convertedTime= userPost.getCreatedAt();
          String dBconvertedTime=format1.format(convertedTime);

          EachPostResponseDto eachPostResponseDto=EachPostResponseDto.builder()
              .id(userPost.getId())
              .title(userPost.getPostTitle())
              .thumbnail(userPost.getPostThumbnail())
              .description(userPost.getPostDescription())
              .tags(tagList)
              .post_url(userPost.getPostUrl())
              .createdAt(dBconvertedTime)
              .memo(userPost.getPostMemo())
              .build();

          return ResponseEntity.status(200).body(eachPostResponseDto);
        }
        return ResponseEntity.status(404).body("");
      }
      return ResponseEntity.status(404).body("");
    }catch(Exception e){
      System.out.println(e);
      return ResponseEntity.status(404).body(" ");
    }

  }
}
