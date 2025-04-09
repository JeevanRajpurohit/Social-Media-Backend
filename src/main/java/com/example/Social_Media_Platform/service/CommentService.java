package com.example.Social_Media_Platform.service;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.Util.PaginationResponse;
import com.example.Social_Media_Platform.dtos.CommentDto;
import com.example.Social_Media_Platform.model.Comment;
import com.example.Social_Media_Platform.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface CommentService {


    @Transactional
    CommentDto createComment(String token, String postId, String content);

    PaginationResponse getCommentsByPostId(String postId, int limit, String lastEvaluatedKey);

    @Transactional
    void deleteComment(String token, String commentId);

    CommentDto mapToCommentDto(Comment comment, User user);
}
