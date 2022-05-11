package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.RefrenceDto;
import com.ewha.devookserver.dto.post.PostBookmarkGetDto;
import com.ewha.devookserver.dto.post.PostBookmarkRequestDto;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.QueryService;
import com.ewha.devookserver.service.RecommendService;
import com.ewha.devookserver.service.TagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class RecommendController {

  private final PostService postService;
  private final OauthService oauthService;
  private final RecommendService recommendService;
  private final UserBookmarkRepository userBookmarkRepository;
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final QueryService queryService;
  private final TagService tagService;


  @GetMapping("/posts")
  public ResponseEntity<List<PostBookmarkRequestDto>> getRecommendList(
      @RequestParam(name = "postId", required = false) Long postId,
      @RequestParam(name = "bookmarkId", required = false) Long bookmarkId,
      @RequestParam(name = "cursor", required = false) Long cursor,
      @RequestParam(name = "limit", required = false) Long limit,
      @RequestParam(name = "q", required = false) String question,
      @RequestParam(name = "tags", required = false) String tags,
      @RequestHeader(name = "Authorization", required = false) String accessTokenGet)
      throws JsonProcessingException {

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

    int isBookmarkInput = 1;
    int isPostInput = 1;

    if (bookmarkId == null) {
      bookmarkId = postId;
      isBookmarkInput = 0;
    }
    if (postId == null) {
      postId = bookmarkId;
      isPostInput = 0;
    }

    // nullpoint error 처리

    if (cursor == null) {
      try {
        cursor = Long.valueOf(100000); // 임의 값 -> 10L 로 수정할지 결정하기
      } catch (Exception e) {
        cursor = Long.valueOf(100000);
      }
    }

    try {

      String accessToken = accessTokenGet.split(" ")[1];
    } catch (Exception e) {
      // accessToken이 빈 값이거나, 존재하지 않을 경우 로그인하지 않은 사용자로 간주

      //유저 추천글 전체 목록 GET (분기)
      if (bookmarkId == null && postId == null) {
        List<Long> postTagList = tagService.makePostTagList(requiredTagList);

        List<PostBookmarkRequestDto> listDtos = postService.responseBookmarkListMakerForNoAuthUser
            (this.queryService.getPostForNotUser(cursor, PageRequest.of(0, 10), "2", question,
                postTagList,
                true, requiredTagList, limit.intValue()));

        return ResponseEntity.status(200).body(
            (listDtos));
      }

    }

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      //유저 추천글 전체 목록 GET (분기)
      if (bookmarkId == null && postId == null) {
        List<Long> postTagList = tagService.makePostTagList(requiredTagList);

        List<PostBookmarkRequestDto> listDtos = postService.responseBookmarkListMakerForNoAuthUser
            (this.queryService.getPostForNotUser(cursor, PageRequest.of(0, 10), "2", question,
                postTagList,
                true, requiredTagList, limit.intValue()));

        return ResponseEntity.status(200).body(
            (listDtos));
      }
    }
    System.out.println(oauthService.isUserExist(accessToken));
    if (!oauthService.isUserExist(accessToken)) {
      //유저 추천글 전체 목록 GET (분기)
      if (bookmarkId == null && postId == null) {
        List<Long> postTagList = tagService.makePostTagList(requiredTagList);

        List<PostBookmarkRequestDto> listDtos = postService.responseBookmarkListMakerForNoAuthUser
            (this.queryService.getPostForNotUser(cursor, PageRequest.of(0, 10), "2", question,
                postTagList,
                true, requiredTagList, limit.intValue()));

        return ResponseEntity.status(200).body(
            (listDtos));
      }
    }    // 유저 예외처리 완료
    String userIdx = oauthService.getUserIdx(accessToken);
    int countAll =
        postRepository.countAllByUserIdx(userIdx) + userBookmarkRepository.countAllByUserIdx(
            Long.valueOf(userIdx));
    if (countAll == 0) {

      if (bookmarkId == null && postId == null) {
        List<Long> postTagList = tagService.makePostTagList(requiredTagList);

        List<PostBookmarkRequestDto> listDtos = postService.responseBookmarkListMakerForNoAuthUser
            (this.queryService.getPostForNotUser(cursor, PageRequest.of(0, 10), "2", question,
                postTagList,
                true, requiredTagList, limit.intValue()));

        return ResponseEntity.status(200).body(
            (listDtos));
      }

    }

    //유저 추천글 전체 목록 GET (분기)
    if (bookmarkId == null && postId == null) {
      List<Long> postTagList = tagService.makePostTagList(requiredTagList);

      List<PostBookmarkRequestDto> listDtos = postService.responseBookmarkListMaker
          (this.queryService.get(cursor, PageRequest.of(0, 10), userIdx, question, postTagList,
              true, requiredTagList, limit.intValue()), userIdx);

      return ResponseEntity.status(200).body(
          (listDtos));
    }

    if (tags == null) {

      Post post = postRepository.getPostByPostIdx(postId);
      List<PostTag> postTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());
      // 해당 글이 가진 태그 리스트 검색

      List<RefrenceDto> refrenceDtos;

      //북마크 아이디는 그대로 함수 사용(calculateRefrence)
      if (isBookmarkInput != 0) {
        refrenceDtos = recommendService.calculateReference(postTagList);
      } else {
        refrenceDtos = recommendService.calculateReferenceOfPost(postTagList);
      }

      List<PostBookmarkRequestDto> listDtos = postService.responseBookmarkListMakerForPost(
          this.queryService.get(cursor, PageRequest.of(0, 10), refrenceDtos, limit.intValue(),
              userIdx, bookmarkId), userIdx);
      List<PostBookmarkRequestDto> resultArrayList = new ArrayList<>();

      for (PostBookmarkRequestDto postListDto : listDtos) {
        if (postListDto.getId() != bookmarkId && postListDto.getId() != postId) {
          resultArrayList.add(postListDto);
        }
      }

      return ResponseEntity.status(200).body(resultArrayList
      );
    }

    return null;
  }

  @GetMapping("/posts/tags")
  public ResponseEntity<List<String>> getRecommendTagList(
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    try {
      String accessToken = accessTokenGet.split(" ")[1];
    } catch (Exception e) {
      List<String> finalResponseString = postService.getPostTagList();
      return ResponseEntity.status(200).body(finalResponseString);
    }
    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      List<String> finalResponseString = postService.getPostTagList();
      return ResponseEntity.status(200).body(finalResponseString);
    }
    System.out.println(oauthService.isUserExist(accessToken));
    if (!oauthService.isUserExist(accessToken)) {
      List<String> finalResponseString = postService.getPostTagList();
      return ResponseEntity.status(200).body(finalResponseString);
    }    // 유저 예외처리 완료
    String userIdx = oauthService.getUserIdx(accessToken);
    System.out.println(userIdx);
    // 1. userIdx와 일치하는 post

    // List<String> finalResponseString = postService.getPostTagList(userIdx);
    List<String> finalResponseString = postService.getPostTagList();

    // 여기 String 배열 반환
    return ResponseEntity.status(200).body(finalResponseString);


  }

  @GetMapping("/posts/{postId}")
  public ResponseEntity<PostBookmarkGetDto> getPost(
      @PathVariable(name = "postId") int bookmarkId,
      @RequestHeader(name = "Authorization") String accessTokenGet) {
    try {

      String accessToken = accessTokenGet.split(" ")[1];
    } catch (ArrayIndexOutOfBoundsException e) {
      // accessToken이 빈 값이거나, 존재하지 않을 경우 로그인하지 않은 사용자로 간주

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

        PostBookmarkGetDto eachPostResponseDto = PostBookmarkGetDto.builder()
            .id(userPost.getId())
            .title(userPost.getPostTitle())
            .thumbnail(userPost.getPostThumbnail())
            .description(userPost.getPostDescription())
            .tags(tagList)
            .url(userPost.getPostUrl())
            .isBookmarked(null) // 이후 수정
            .build();

        return ResponseEntity.status(200).body(eachPostResponseDto);
      }
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    String accessToken = accessTokenGet.split(" ")[1];

    if (!oauthService.validatieTokenInput(accessToken)) {
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

        PostBookmarkGetDto eachPostResponseDto = PostBookmarkGetDto.builder()
            .id(userPost.getId())
            .title(userPost.getPostTitle())
            .thumbnail(userPost.getPostThumbnail())
            .description(userPost.getPostDescription())
            .tags(tagList)
            .url(userPost.getPostUrl())
            .isBookmarked(null) // 이후 수정
            .build();

        return ResponseEntity.status(200).body(eachPostResponseDto);
      }
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }
    System.out.println(oauthService.isUserExist(accessToken));
    if (!oauthService.isUserExist(accessToken)) {
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

        PostBookmarkGetDto eachPostResponseDto = PostBookmarkGetDto.builder()
            .id(userPost.getId())
            .title(userPost.getPostTitle())
            .thumbnail(userPost.getPostThumbnail())
            .description(userPost.getPostDescription())
            .tags(tagList)
            .url(userPost.getPostUrl())
            .isBookmarked(null) // 이후 수정
            .build();

        return ResponseEntity.status(200).body(eachPostResponseDto);
      }
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);

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

      boolean getIsBookmarked = recommendService.checkIsBookmarked((long) bookmarkId, userIdx);

      PostBookmarkGetDto eachPostResponseDto = PostBookmarkGetDto.builder()
          .id(userPost.getId())
          .title(userPost.getPostTitle())
          .thumbnail(userPost.getPostThumbnail())
          .description(userPost.getPostDescription())
          .tags(tagList)
          .url(userPost.getPostUrl())
          .isBookmarked(getIsBookmarked) // 이후 수정
          .build();

      return ResponseEntity.status(200).body(eachPostResponseDto);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);

  }
}
