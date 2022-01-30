package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Notification;
import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.dto.post.EachPostResponseDto;
import com.ewha.devookserver.dto.post.PostLabmdaRequestDto;
import com.ewha.devookserver.dto.post.PostLambdaDto;
import com.ewha.devookserver.dto.post.PostUserRequestDto;
import com.ewha.devookserver.dto.post.RequestMemoDto;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.ewha.devookserver.service.NotificationService;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
public class PostController {

  private final PostService postService;
  private final OauthService oauthService;
  private final PostRepository postRepository;
  private final UserBookmarkRepository userBookmarkRepository;
  private final NotificationService notificationService;

  @PostMapping("/bookmarks")
  public ResponseEntity<?> testPost(
      @RequestHeader(value = "Authorization") String tokenGet,
      HttpServletResponse response, @RequestBody PostUserRequestDto postUserRequestDto) {

    System.out.println(postUserRequestDto.getMemo());
    System.out.println(postUserRequestDto.getPostId());

    try {
      System.out.println(tokenGet + "요청");
      String accessToken = tokenGet.split(" ")[1];
      if (Objects.equals(accessToken, "undefined")) {
        return ResponseEntity.status(401).body("1");
      }

      // 존재하지 않는 유저
      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(401).body("2");
      }
      String userIdx = oauthService.getUserIdx(accessToken);

      // 만약 ID 값이 정확히 존재한다면. (null 값으로 판별)
      if (postUserRequestDto.getPostId() != null) {
        Long postIdx = Long.valueOf(postUserRequestDto.getPostId());
        if (postRepository.existsByPostIdx(postIdx)) {
          if (userBookmarkRepository.existsByPost_postIdxAndUser_userIdx(
              Long.valueOf(postUserRequestDto.getPostId()), Long.valueOf(userIdx)) != null) {

            if (postRepository.existsByPostIdxAndUserIdx(
                Long.valueOf(postUserRequestDto.getPostId()),
                userIdx)) {
              System.out.println("이미 유저가 등록한 post, 따로 북마크 되지는 않음"); // 일단 이렇게 처리하고, 나중에 고려
              return ResponseEntity.status(201).body("DB에는 생성하지 않고, 201 코드 리턴");
            }
            System.out.println("이미 등록된 북마크.");
            return ResponseEntity.status(201).body("DB에는 생성하지 않고, 201 코드 리턴");
          } else {
            Post post = postRepository.getPostByPostIdx(postIdx);
            post.setUserIdx(userIdx);
            postService.savePostBookmark(Long.valueOf(userIdx), postIdx);
          }
          return ResponseEntity.status(201).body("북마크 새로 생성완료!");

        }
        // postIdx 에 해당하는 post가 POST table에 존재하지 않을 때
        return ResponseEntity.status(401).body("입력한 postId가 post table에 존재하지 않음");

      }

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

      return ResponseEntity.status(201).body("4");
    } catch (Exception e) {
      System.out.println(e);
      return ResponseEntity.status(500).body("5");
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

    } catch (Exception e) {
      System.out.println(e);
      return ResponseEntity.status(401).body(" ");
    }

  }


/*
  @GetMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<?> eachPostResponse(
      @PathVariable(name = "bookmarkId") int bookmarkId,
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

      if (postRepository.existsByPostIdx((long) bookmarkId)) {
        Post userPost = postRepository.getPostByPostIdx((long) bookmarkId);

        List<String> tagList = postService.getEachPostTagList(bookmarkId);
        if (tagList.size() == 0) {
          tagList.add("태그1");
          tagList.add("태그2");
        }

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date convertedTime = userPost.getCreatedAt();
        String dBconvertedTime = format1.format(convertedTime);

        EachPostResponseDto eachPostResponseDto = EachPostResponseDto.builder()
            .id(userPost.getId())
            .title(userPost.getPostTitle())
            .thumbnail(userPost.getPostThumbnail())
            .description(userPost.getPostDescription())
            .tags(tagList)
            .url(userPost.getPostUrl())
            .createdAt(dBconvertedTime)
            .memo(userPost.getPostMemo())
            .build();

        return ResponseEntity.status(200).body(eachPostResponseDto);
      }
      return ResponseEntity.status(404).body("");
    } catch (Exception e) {
      System.out.println(e);
      return ResponseEntity.status(404).body(" ");
    }

  }

 */

  // 개별 북마크 글 조회 GET
  @GetMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<?> eachPostResponse(
      @PathVariable(name = "bookmarkId") int bookmarkId,
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

      if (postRepository.existsByPostIdx((long) bookmarkId)) {
        Post userPost = postRepository.getPostByPostIdx((long) bookmarkId);

        List<String> tagList = postService.getEachPostTagList(bookmarkId);
        if (tagList.size() == 0) {
          tagList.add("태그1");
          tagList.add("태그2");
        }

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date convertedTime = userPost.getCreatedAt();
        String dBconvertedTime = format1.format(convertedTime);

        LocalDateTime dueDate = null;
        Optional<Notification> notification=notificationService.returnDueDate((long)bookmarkId,Long.valueOf(userIdx),true);
        if(!notification.isEmpty()){
          dueDate=notification.get().getDueDate();
        }

        EachPostResponseDto eachPostResponseDto = EachPostResponseDto.builder()
            .id(userPost.getId())
            .title(userPost.getPostTitle())
            .thumbnail(userPost.getPostThumbnail())
            .description(userPost.getPostDescription())
            .tags(tagList)
            .url(userPost.getPostUrl())
            .createdAt(dBconvertedTime)
            .memo(userPost.getPostMemo())
            .isRead(userPost.getIsRead()) //isRead, dueDate 추가
            .dueDate(dueDate)
            .build();

        return ResponseEntity.status(200).body(eachPostResponseDto);
      }
      return ResponseEntity.status(404).body("");
    } catch (Exception e) {
      System.out.println(e);
      return ResponseEntity.status(404).body(" ");
    }

  }

  @PatchMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<?> editBookmarkMemo(
      @PathVariable(name = "bookmarkId") int bookmarkId,
      @RequestHeader(name = "Authorization") String accessTokenGet,
      @RequestBody RequestMemoDto requestMemoDto) {

    String requestMemo = requestMemoDto.getMemo();
    Date requestDueDate = requestMemoDto.getDueDate();
    Boolean isRead = requestMemoDto.getIsRead();

    try {
      String accessToken = accessTokenGet.split(" ")[1];
      if (!oauthService.validatieTokenInput(accessToken)) {
        return ResponseEntity.status(401).body(" ");
      }
      System.out.println(oauthService.isUserExist(accessToken));
      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(401).body(" ");
      }    // 유저 예외처리 완료

      // 이제 post 관련한 method 짜기.

      String userIdx = oauthService.getUserIdx(accessToken);
      if (postRepository.existsByPostIdx((long) bookmarkId)) {
        if (postRepository.getPostByPostIdx(Long.valueOf(bookmarkId)).getUserIdx()
            .equals(userIdx)) {

          Post newPost = postRepository.getPostByPostIdx(Long.valueOf(bookmarkId));
          newPost.setPostMemo(requestMemo);
          postRepository.save(newPost);

          return ResponseEntity.status(200).body(" ");
        }
      }
      return ResponseEntity.status(401).body(" ");


    } catch (Exception e) {
      System.out.println(e);
      return ResponseEntity.status(401).body(" ");
    }

  }
}
