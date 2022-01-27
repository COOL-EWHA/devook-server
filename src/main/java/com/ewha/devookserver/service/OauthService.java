package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.auth.JwtTokenProvider;
import com.ewha.devookserver.domain.auth.LoginResponse;
import com.ewha.devookserver.domain.auth.OauthAttributes;
import com.ewha.devookserver.domain.auth.OauthProvider;
import com.ewha.devookserver.domain.auth.OauthTokenResponse;
import com.ewha.devookserver.domain.auth.UserProfile;
import com.ewha.devookserver.domain.user.Member;
import com.ewha.devookserver.dto.auth.RefreshDto;
import com.ewha.devookserver.repository.InMemoryProviderRepository;
import com.ewha.devookserver.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OauthService {

  private final InMemoryProviderRepository inMemoryProviderRepository;
  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;

  public OauthService(InMemoryProviderRepository inMemoryProviderRepository,
      MemberRepository memberRepository,
      JwtTokenProvider jwtTokenProvider) {
    this.inMemoryProviderRepository = inMemoryProviderRepository;
    this.memberRepository = memberRepository;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  public boolean validatieTokenInput(String accessToken) {
    try {
      jwtTokenProvider.getPayload(accessToken);
    } catch (RuntimeException e) {
      return false;
    }
    return true;
  }

  public String getAccessToken(Member member) {
    return jwtTokenProvider.createAccessToken(String.valueOf(member.getId()));
  }

  public RefreshDto refreshUserToken(Member member) {
    if (member == null) {
      System.out.println("\n\n\nnull");
    }

    Member willChangeMember = memberRepository.findMemberById(member.getId());

    String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(member.getId()));
    String refreshToken = member.getRefreshToken();

    if (jwtTokenProvider.validateCheckWeekToken(member.getRefreshToken())) {
      refreshToken = jwtTokenProvider.createRefreshToken();
      willChangeMember.updateRefreshToken(refreshToken);
      memberRepository.save(willChangeMember);
    }
    return RefreshDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .member(willChangeMember)
        .build();
  }


  public String getUserIdx(String accessToken) {
    String exampleResult = jwtTokenProvider.getPayload(accessToken);
    return exampleResult;
  }

  public void deleteUserRefreshToken(Long userIdx) {
    Member deletedMember = memberRepository.findMemberById(userIdx);
    deletedMember.setRefreshToken("");
    memberRepository.save(deletedMember);

  }


  public boolean isUserExist(String accessToken) {
    String exampleResult = jwtTokenProvider.getPayload(accessToken);
    Long convertedIdx = Long.valueOf(exampleResult);
    return memberRepository.existsById(convertedIdx);
  }


  // 최초 회원가입 & 로그인
  public LoginResponse login(String providerName, String code) {
    OauthProvider provider = inMemoryProviderRepository.findByProviderName(providerName);
    OauthTokenResponse tokenResponse = getToken(code, provider);
    UserProfile userProfile = getUserProfile(providerName, tokenResponse, provider);

    // 이미 존재하는 유저인지 확인 (회원가입 or 로그인)
    boolean existUser = memberRepository.existsMemberByOauthId(userProfile.getOauthId());

      String refreshToken = jwtTokenProvider.createRefreshToken();
    Member member = saveOrUpdate(userProfile, refreshToken);
    String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(member.getId()));

    return LoginResponse.builder()
        .id(member.getId())
        .nickname(member.getName())
        .email(member.getEmail())
        .imageUrl(member.getImageUrl())
        .role(member.getRole())
        .tokenType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .isExistUser(existUser)
        .build();
  }


  private Member saveOrUpdate(UserProfile userProfile, String refreshToken) {

    Member member = memberRepository.findByOauthId(userProfile.getOauthId())
        .map(entity -> entity.update(userProfile.getName(), userProfile.getEmail(),
            userProfile.getImageUrl(), refreshToken))
        .orElseGet(() -> userProfile.toMember(refreshToken));
    return memberRepository.save(member);
  }

  private OauthTokenResponse getToken(String code, OauthProvider provider) {
    return WebClient.create()
        .post()
        .uri(provider.getTokenUrl())
        .headers(header -> {
          header.setBasicAuth(provider.getClientId(), provider.getClientSecret());
          header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
          header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
          header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        })
        .bodyValue(tokenRequest(code, provider))
        .retrieve()
        .bodyToMono(OauthTokenResponse.class)
        .block();
  }

  private MultiValueMap<String, String> tokenRequest(String code, OauthProvider provider) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("code", code);
    formData.add("grant_type", "authorization_code");
    formData.add("redirect_uri", provider.getRedirectUrl());
    return formData;
  }

  private UserProfile getUserProfile(String providerName, OauthTokenResponse tokenResponse,
      OauthProvider provider) {
    Map<String, Object> userAttributes = getUserAttributes(provider, tokenResponse);
    return OauthAttributes.extract(providerName, userAttributes);
  }

  private Map<String, Object> getUserAttributes(OauthProvider provider,
      OauthTokenResponse tokenResponse) {
    return WebClient.create()
        .get()
        .uri(provider.getUserInfoUrl())
        .headers(header -> header.setBearerAuth(tokenResponse.getAccessToken()))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
        })
        .block();
  }
}
