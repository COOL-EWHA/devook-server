package com.ewha.devookserver.service;

import com.ewha.devookserver.dto.post.PostLabmdaRequestDto;
import com.ewha.devookserver.dto.post.PostLambdaDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CrawlerService {

  WebClient webClient = WebClient.create(
      "https://sy54a2wnyl.execute-api.ap-northeast-2.amazonaws.com/test");


  // GET /surfit/categories
  public List<String> getCategoryInfo()
      throws JsonProcessingException {

    JsonNode result = webClient.post()
        .uri("/test")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .bodyToMono(String.class).map(s -> {
          ObjectMapper mapper = new ObjectMapper();
          try {
            return mapper.readTree(s);
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
          return null;
        })
        .block();

    ObjectMapper objectMapper = new ObjectMapper();
    if (result != null) {
      String returnValue = objectMapper.writeValueAsString(result);
      List<String> categoryList = objectMapper.readValue(returnValue, List.class);
      return categoryList;
    }
    return null;
  }
}
