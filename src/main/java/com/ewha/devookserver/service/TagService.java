package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.user.UserBookmark;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TagService {

  private final TagRepository tagRepository;
  private final QueryRepository queryRepository;
  private final PostRepository postRepository;
  private final UserBookmarkRepository userBookmarkRepository;






  // 해당 태그를 가지고 있는 모든 리스트 목록을 반환하는 함수.
  public List<Long> makePostTagList(List<String> tagList){

    List<Long> postIdxList=new ArrayList<>();
    for(String tagInput:tagList){

      List<PostTag> postTagList = tagRepository.findAllByPostTagName(tagInput);

      for(PostTag postTag:postTagList){
        if(!postIdxList.contains(postTag.getPost_postIdx())){
          postIdxList.add(postTag.getPost_postIdx().longValue());
        }
      }
    }

    return postIdxList;
  }



}
