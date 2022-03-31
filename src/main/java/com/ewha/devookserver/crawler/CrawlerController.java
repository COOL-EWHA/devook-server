package com.ewha.devookserver.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrawlerController {

  @PostMapping("/get")
  public void getCrawlerList(String requestListDto) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    RequestListDto[] requestListDtoList = mapper.readValue(requestListDto, RequestListDto[].class);

    for (RequestListDto requestListDto1 : requestListDtoList) {
      System.out.println(requestListDto1.getTitle());
    }

  }
}
