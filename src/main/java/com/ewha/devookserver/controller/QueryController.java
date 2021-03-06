package com.ewha.devookserver.controller;

import com.ewha.devookserver.dto.post.PostListDto;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.QueryService;
import com.ewha.devookserver.service.TagService;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class QueryController {

  private final PostService postService;
  private final OauthService oauthService;
  private final QueryService queryService;
  private final TagService tagService;


  @GetMapping("/bookmarks")
  public ResponseEntity<List<PostListDto>> getBookmarkList
      (@RequestParam(name = "tags", required = false) String tags,
          @RequestParam(name = "cursor", required = false) Long cursor,
          @RequestParam(name = "q", required = false) String question,
          @RequestParam(name = "isRead", required = false) Boolean isRead,
          @RequestHeader(value = "Authorization") String tokenGet
      ) {

    List<String> requiredTagList = new ArrayList<>();

    if (tags != null) {
      StringTokenizer tokens = new StringTokenizer(tags, ",");

      while (tokens.hasMoreTokens()) {
        requiredTagList.add(tokens.nextToken());
      }

    }
    int limit = 10;

    try {
      String accessToken = tokenGet.split(" ")[1];

      // 로그인 안 한 유저
      if (accessToken == "undefined") {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
      }
      // 존재하지 않는 유저
      if (!oauthService.isUserExist(accessToken)) {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
      }

      String userIdx = oauthService.getUserIdx(accessToken);

      List<Long> postTagList = tagService.makePostTagList(requiredTagList);

      // 11.21 @ 1:03:31 수정사항

      if (cursor == null) {
        try {
          cursor = 1000000L;//사용자의 가장 최근 글 값
        } catch (Exception e) {
          cursor = Long.valueOf(1);
        }

      }

      if (tags == null) {
        // 여기 아래부터 시작

        return ResponseEntity.status(200).body(postService.responseListMaker
            (this.queryService.get(cursor, PageRequest.of(0, 10), userIdx, question), isRead,
                Long.valueOf(userIdx)));
      }

      // 여기 아래부터 시작
      return ResponseEntity.status(200).body(postService.responseListMaker
          (this.queryService.get(cursor, PageRequest.of(0, 10), userIdx, question, postTagList),
              isRead, Long.valueOf(userIdx)));


    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

  }


}
