package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.repository.PostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BasicListService {
  private final PostRepository postRepository;
  public List<Post> getBasicPostList(){
    return postRepository.returnBasicList();
  }
}
