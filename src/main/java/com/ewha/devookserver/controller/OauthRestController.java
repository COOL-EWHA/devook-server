package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.auth.JwtTokenProvider;
import com.ewha.devookserver.domain.auth.LoginResponse;
import com.ewha.devookserver.domain.user.Member;
import com.ewha.devookserver.dto.auth.LoginFinalResponseDto;
import com.ewha.devookserver.dto.auth.RefreshDto;
import com.ewha.devookserver.dto.auth.RefreshResponseDto;
import com.ewha.devookserver.dto.auth.RevisedCookieDto;
import com.ewha.devookserver.dto.auth.TestLoginDto;
import com.ewha.devookserver.dto.auth.TokenRequestDto;
import com.ewha.devookserver.dto.auth.UserInfoResponseDto;
import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.UserService;
import java.util.Objects;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OauthRestController {

  private final OauthService oauthService;
  private final UserService userService;
  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;

  // @ 0217 09:45 변경사항 production ver.

  @PostMapping("/auth/logout")
  public ResponseEntity<?> userLogout(@RequestHeader(value = "Authorization") String accessTokenGet,
      HttpServletResponse response) throws Exception {

    String accessToken = accessTokenGet.split(" ")[1];

    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    System.out.println(oauthService.isUserExist(accessToken));

    try {
      if (!oauthService.isUserExist(accessToken)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      String userIdx = oauthService.getUserIdx(accessToken);
      // oauthService.deleteUserRefreshToken(Long.valueOf(userIdx));

      ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", null)
          .maxAge(0)
          .httpOnly(true)
          .path("/")
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (RuntimeException e) {
      System.out.println(e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/auth/test-login")
  public ResponseEntity<?> testLogin(HttpServletRequest request, HttpServletResponse response,
      @RequestBody TestLoginDto testLoginDto) {

    String refreshToken = testLoginDto.getRefreshToken();
    boolean isUserExistRefreshToken = userService.isMemberExistByUserRefreshToken(refreshToken);

    if (isUserExistRefreshToken) {
        Member member = memberRepository.findMemberByRefreshToken(refreshToken);
        String userAccessToken = oauthService.getAccessToken(member);

        LoginFinalResponseDto loginFinalResponseDto = LoginFinalResponseDto.builder()
            .email(member.getEmail())
            .nickname(member.getName())
            .accessToken(userAccessToken)
            .refreshToken(member.getRefreshToken())
            .build();

        RevisedCookieDto revisedCookieDto = RevisedCookieDto.builder()
            .email(loginFinalResponseDto.getEmail())
            .nickname(loginFinalResponseDto.getNickname())
            .accessToken(loginFinalResponseDto.getAccessToken())
            .build();

        ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
                loginFinalResponseDto.getRefreshToken())
            .httpOnly(true)
            .path("/")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        //    .domain(".example.com")

        return ResponseEntity.status(200).body(revisedCookieDto);
      } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<?> loginRefresh(@RequestHeader(value = "Cookie") String refreshTokenGet,
      HttpServletRequest request,
      HttpServletResponse response) {

    String accessToken = "no";
    Cookie[] list = request.getCookies();
    for(Cookie cookie : list){
      if(cookie.getName().equals("REFRESH_TOKEN")){
        accessToken = cookie.getValue();
      }
    }
    System.out.println(accessToken);

    try {

      if (Objects.equals(refreshTokenGet, "REFRESH_TOKEN=")) {
        /*
        ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", null)
            .sameSite("None")
            .secure(true)
            .httpOnly(true)
            .path("/")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        System.out.println("빈 리프레쉬");

         */
        System.out.println("빈 리프레쉬");
        return ResponseEntity.status(404).body("빈리프레쉬");
      }


      boolean isTokenExists = userService.checkRightRefreshToken(accessToken);

      if (!isTokenExists) {
        /*
        ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", null)
            .sameSite("None")
            .secure(true)
            .httpOnly(true)
            .path("/")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        System.out.println("토큰 존재하지 않음");


         */

        System.out.println("토큰이 존재하지 않을 경우");
        return ResponseEntity.status(404).body("토큰이 존재하지 않을 경우");


      } else {
        Member member = userService.returnRefreshTokenMember(accessToken);

        RefreshDto refreshDto = oauthService.refreshUserToken(member);
        RefreshResponseDto refreshResponseDto =
            RefreshResponseDto.builder().accessToken(refreshDto.getAccessToken())
                .refreshToken(refreshDto.getRefreshToken()).build();
        ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
                refreshDto.getRefreshToken())
            .httpOnly(true)
            .path("/")
            .build();
        //domain "localhost:8080"
        //domain "localhost:3000"
        // domain "https://localhost:3000"

        int i = 0;
        Cookie[] getCookie = request.getCookies();
        for (i = 0; i < getCookie.length; i++) {
          Cookie c = getCookie[i];
          String name = c.getName();
          String value = c.getValue();
        }
        //if(getCookie==null){

        //response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        //}
        System.out.println("정상적으로 완료");
        return ResponseEntity.status(HttpStatus.OK).body(refreshResponseDto);
      }

    } catch (Exception e) {
      /*
      ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", null)
          .sameSite("None")
          .secure(true)
          .httpOnly(true)
          .path("/")
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

       */

      System.out.println("오류");

      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(" ");
    }
  }


  @PostMapping("/auth/login/{provider}")
  public ResponseEntity<?> loginUser(@PathVariable String provider,
      @RequestBody TokenRequestDto tokenRequestDto, HttpServletResponse response) {
    System.out.println("@PostMapping /auth/login/provider");

    String replaceToken = tokenRequestDto.getCode().replace("%2F", "/");
    System.out.println(tokenRequestDto.getCode());

    try {
      LoginResponse loginResponse = oauthService.login(provider, replaceToken);

      LoginFinalResponseDto loginFinalResponseDto = LoginFinalResponseDto.builder()
          .email(loginResponse.getEmail())
          .nickname(loginResponse.getNickname())
          .accessToken(loginResponse.getAccessToken())
          .refreshToken(loginResponse.getRefreshToken())
          .build();

      RevisedCookieDto revisedCookieDto = RevisedCookieDto.builder()
          .email(loginResponse.getEmail())
          .nickname(loginResponse.getNickname())
          .accessToken(loginResponse.getAccessToken())
          .build();

      ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
              loginFinalResponseDto.getRefreshToken())
          .httpOnly(true)
          .path("/")
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

      if (!loginResponse.isExistUser()) {
        return ResponseEntity.status(201).body(revisedCookieDto);
      }

      return ResponseEntity.status(200).body(revisedCookieDto);
    } catch (Exception e) {
      System.out.println(e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/users")
  public ResponseEntity<?> getUserInfo(
      @RequestHeader(value = "Authorization") String accessTokenGet, HttpServletResponse response)
      throws Exception {
    System.out.println("@GetMapping /users");
    String accessToken = accessTokenGet.split(" ")[1];

    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      if (!oauthService.isUserExist(accessToken)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      String userIdx = oauthService.getUserIdx(accessToken);

      System.out.println(userIdx);
      Long convertedIdx = Long.valueOf(userIdx);
      Member member = userService.findMemberInfo(convertedIdx);

      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add("Set-Cookie", accessToken);
      UserInfoResponseDto userinfo = UserInfoResponseDto.builder()
          .email(member.getEmail())
          .nickname(member.getName())
          .build();
      return ResponseEntity.status(200).headers(httpHeaders).body(userinfo);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/users")
  public ResponseEntity<?> deleteUser(
      @RequestHeader(value = "Authorization") String accessTokenGet) {
    String accessToken = accessTokenGet.split(" ")[1];

    try {
      if (!oauthService.isUserExist(accessToken)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      String userIdx = oauthService.getUserIdx(accessToken);
      Long convertedIdx = Long.valueOf(userIdx);
      Member member = userService.findMemberInfo(convertedIdx);

      userService.deleteMember(member.getId());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
