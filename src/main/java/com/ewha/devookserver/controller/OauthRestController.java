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


  @PostMapping("/auth/logout")
  public ResponseEntity<?> userLogout(@RequestHeader(value = "Authorization") String accessTokenGet,
      HttpServletResponse response) throws Exception {

    System.out.println("@GetMapping /auth/logout");

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
      oauthService.deleteUserRefreshToken(Long.valueOf(userIdx));

      ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", null)
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
  public ResponseEntity<?> testLogin(HttpServletResponse response,
      @RequestBody TestLoginDto testLoginDto) {

    String userEmail = testLoginDto.getEmail();
    boolean isUserExistemail = userService.isMemberExistByEmail(userEmail);

    //유저가 존재하면 그 유저의 정보 반환

    if (isUserExistemail) {
      if (memberRepository.countMemberByEmail(userEmail) != 1) {
        Member member = userService.ifGoogleUser(userEmail);
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
        Member member = userService.returnEmailUSer(userEmail);
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

        return ResponseEntity.status(200).body(revisedCookieDto);
      }
    } else {
      System.out.println("일치하는 유저가 없습니다");
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<?> loginRefresh(
      @RequestHeader(value = "Cookie") String realRefreshToken, HttpServletResponse response) {

    try {

      String tokenizedRefreshToken = null;

      String[] date = realRefreshToken.split("; ");

      for (int i = 0; i < date.length; i++) {
        System.out.println("번호" + date[i]);
        if (date[i].contains("REFRESH_TOKEN")) {
          tokenizedRefreshToken = date[i];
        }
      }

      System.out.println(tokenizedRefreshToken);

      if (Objects.equals(tokenizedRefreshToken, "REFRESH_TOKEN=")) {
        ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
                null)
            .httpOnly(true)
            .path("/")
            .build();
        //domain "localhost:8080"
        //domain "localhost:3000"
        // domain "https://localhost:3000"
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.status(404).body("");
      }

      String refreshTokenGet = tokenizedRefreshToken;
      String accessToken = refreshTokenGet.split("=")[1];

      System.out.println(accessToken + "\n\n\n\n\n");

      System.out.println("@Getmapping /auth/refresh");
      boolean isTokenExists = userService.checkRightRefreshToken(accessToken);
      System.out.println(isTokenExists);

      if (!isTokenExists) {
        System.out.println("존재하지 않는 유저!\n");
        ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
                null)
            .httpOnly(true)
            .path("/")
            .build();
        //domain "localhost:8080"
        //domain "localhost:3000"
        // domain "https://localhost:3000"
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.status(404).body("");
      } else {
        System.out.println("존재하는 유저입니다");
        Member member = userService.returnRefreshTokenMember(accessToken);
        System.out.println(member.getRefreshToken());

        // refreshToken 값이 업데이트가 안됨. accessToken 값은 매번 새롭게 잘 설정되고 있음.

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
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.status(HttpStatus.OK).body(refreshResponseDto);
      }
    } catch (Exception e) {
      System.out.println("오류 발생 Exception::::");
      System.out.println(e);
      ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
              null)
          .httpOnly(true)
          .path("/")
          .build();
      //domain "localhost:8080"
      //domain "localhost:3000"
      // domain "https://localhost:3000"
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(" ");
    }
  }

  @PostMapping("/auth/login/{provider}")
  public ResponseEntity<?> loginUser(@PathVariable String provider,
      @RequestBody TokenRequestDto tokenRequestDto, HttpServletResponse response) {
    System.out.println("@PostMapping /auth/login/provider");

    String replaceToken = tokenRequestDto.getCode().replace("%2F", "/");
    System.out.println(replaceToken);

    try {
      LoginResponse loginResponse = oauthService.login(provider, replaceToken);
      // 기존 회원 아님 -> 회원가입 진행 (201)

      //Error : 처음 회원가입 할때 refreshToken이 저장이 안된다.

      System.out.println(loginResponse.isExistUser());
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
      System.out.println("에러내용");
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

    System.out.println(oauthService.isUserExist(accessToken));

    try {
      if (!oauthService.isUserExist(accessToken)) {
        System.out.println("존재하지 않는 유저");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      System.out.println("여기까지 ok");
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
      System.out.println("유효하지 않은 토큰");
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
      System.out.println("삭제 오류 발생. 존재하지 않는 유저");
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
