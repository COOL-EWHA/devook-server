package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.dto.PostListDto;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.user.UserBookmark;
import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostLabmdaRequestDto;
import com.ewha.devookserver.domain.post.PostLambdaDto;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PostService {
  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final QueryRepository queryRepository;
  private final UserBookmarkRepository userBookmarkRepository;


  public boolean isPostUserExists(String url, String userIdx){
    if(postRepository.getPostByPostUrlAndUserIdx(url, userIdx)!=null){
      return true;
    }
    return false;
  }



  public List<Post> getTestPage(){
    return postRepository.findWithPagination(Pageable.ofSize(5));
  }


  public List<String> getEachPostTagList(int id){
    List<PostTag> postTagList=queryRepository.findAllTagsByPost(id);
    List<String> searchResponseDtoList=new ArrayList<>();

    for(PostTag postTag:postTagList){
      searchResponseDtoList.add(postTag.getPostTagName());
    }

    return searchResponseDtoList;
  }


  public List<String> getPostTagList(String userIdx){
    List<Post> returnPost= postRepository.findAllByUserIdx(userIdx);

    List<String> searchResponseDtoList=new ArrayList<>();

    for(Post post : returnPost){
      List<PostTag> postTagList=queryRepository.findAllTagsByPost(post.getPostIdx().intValue());

      for(PostTag postTag:postTagList){
        if(!searchResponseDtoList.contains(postTag.getPostTagName())){
          searchResponseDtoList.add(postTag.getPostTagName());
        }
      }
    }

    return searchResponseDtoList;

  }

  public List<String> getPostTagList(){
    List<Post> returnPost= postRepository.findAll();

    List<String> searchResponseDtoList=new ArrayList<>();

    for(Post post : returnPost){
      List<PostTag> postTagList=queryRepository.findAllTagsByPost(post.getPostIdx().intValue());

      for(PostTag postTag:postTagList){
        if(!searchResponseDtoList.contains(postTag.getPostTagName())){
          searchResponseDtoList.add(postTag.getPostTagName());
        }
      }
    }

    return searchResponseDtoList;

  }

  public List<PostListDto> responseListMaker(CursorResult<Post> productList){
    List<PostListDto> searchResponseDtoList=new ArrayList<>();


    for(Post post : productList.getValues()){
      List<String> forTestString=new ArrayList<>();
      List<PostTag> postTagList=queryRepository.findAllTagsByPost(post.getPostIdx().intValue());

      for(PostTag postTag : postTagList){
        forTestString.add(postTag.getPostTagName());
      }

      if(forTestString.size()==0){
        forTestString.add("태그1");
        forTestString.add("태그2");
      }

        PostListDto postListDto = PostListDto.builder()
            .id(post.getId())
            .thumbnail(post.getPostThumbnail())
            .description(post.getPostDescription())
            .title(post.getPostTitle())
            .tags(forTestString)
            .url(post.getPostUrl())
            .build();
        searchResponseDtoList.add(postListDto);
    }
    return searchResponseDtoList;
  }

  public CursorResult<Post> get(Long cursorId, Pageable page, String userIdx, String question){
    final List<Post> boards=getPost(cursorId,page, userIdx, question);
    final Long lastIdofList=boards.isEmpty()?
        null:boards.get(boards.size()-1).getId();

    return new CursorResult<>(boards, hasNext(lastIdofList));
  }

  public List<Post> getPost(Long id, Pageable page, String userIdx, String question){
    return id == null ?
        postRepository.findAllByPostIdx(page, userIdx):
        postRepository.findAllByPostIdxDesc(id, page, userIdx);
  }

  public Boolean hasNext(Long id) {
    if (id == null) return false;
    return this.postRepository.existsByPostIdx(id);
  }



  WebClient webClient = WebClient.create("https://sy54a2wnyl.execute-api.ap-northeast-2.amazonaws.com/test");

  public boolean deletePost(int postIdx, String userIdx){
    if(postRepository.existsByPostIdx((long) postIdx)){
      if(Objects.equals(postRepository.getPostByPostIdx((long) postIdx).getUserIdx(), userIdx)){
        postRepository.deletePostByPostIdx((long)postIdx);
        return true;
      }
      else{
        return false;
      }
    }
    return false;
  }

  public void savePost(String memo, String url, String description, String title, String image, String userIdx){

    Post post = Post.builder()
        .postMemo(memo)
        .postUrl(url)
        .postDescription(description)
        .postThumbnail(image)
        .postTitle(title)
        .userIdx(userIdx)
        .build();

    postRepository.save(post);
  }
  public void savePostBookmark(Long user_userIdx, Long post_postIdx){

    UserBookmark userBookmark=UserBookmark.builder()
            .post_postIdx(post_postIdx)
                .user_userIdx(user_userIdx)
                    .build();

    userBookmarkRepository.save(userBookmark);
  }
  public boolean isPostExists(String url){
    return postRepository.existsByPostUrl(url);
  }


  // lambda 함수 읽어오는 부분 ( web-crawler ) WebClient 사용
  public PostLambdaDto getPostInfo(PostLabmdaRequestDto postLabmdaRequestDto)
      throws JsonProcessingException {

    JsonNode result = webClient.post()
        .uri("/test")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(Mono.just(postLabmdaRequestDto), PostLabmdaRequestDto.class)
        .retrieve()
        .bodyToMono(String.class).map(s->{
          ObjectMapper mapper = new ObjectMapper();
          try {
            return mapper.readTree(s);
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
          return null;
        })
            .block();

    System.out.println(result);

     ObjectMapper objectMapper = new ObjectMapper();
     if(result!=null){
       String returnValue = objectMapper.writeValueAsString(result);
       PostLambdaDto postLambdaDto = objectMapper.readValue(returnValue, PostLambdaDto.class);
       return postLambdaDto;
     }
     return null;
  }
}
