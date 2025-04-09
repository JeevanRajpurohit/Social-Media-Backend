package com.example.Social_Media_Platform.service;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.Util.PaginationResponse;
import com.example.Social_Media_Platform.dtos.PostDto;
import com.example.Social_Media_Platform.model.Post;
import com.example.Social_Media_Platform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PostService {

    @Transactional
    PostDto createPost(String userId, String content, MultipartFile image) throws IOException;


    PostDto getPostById(String postId);

    PaginationResponse getPostsByUserId(String userId, int limit, String lastEvaluatedKey);

    List<PostDto> getNewsFeed(String token, int limit);

    @Transactional
    void deletePost(String token, String postId);

    @Transactional
    PostDto likePost(String token, String postId);

    @Transactional
    PostDto unlikePost(String token, String postId);

    PostDto mapToPostDto(Post post, User user);
}
