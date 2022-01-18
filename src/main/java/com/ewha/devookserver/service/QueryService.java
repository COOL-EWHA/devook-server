package com.ewha.devookserver.service;

import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QueryService {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;



}
