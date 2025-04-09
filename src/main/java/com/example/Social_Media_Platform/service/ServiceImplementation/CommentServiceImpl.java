package com.example.Social_Media_Platform.service.ServiceImplementation;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import com.example.Social_Media_Platform.Util.DateConverter;
import com.example.Social_Media_Platform.Util.PaginationResponse;
import com.example.Social_Media_Platform.dtos.CommentDto;
import com.example.Social_Media_Platform.exception.PostNotFoundException;
import com.example.Social_Media_Platform.exception.UserNotFoundException;
import com.example.Social_Media_Platform.model.Comment;
import com.example.Social_Media_Platform.model.Post;
import com.example.Social_Media_Platform.model.User;
import com.example.Social_Media_Platform.repository.CommentRepository;
import com.example.Social_Media_Platform.repository.PostRepository;
import com.example.Social_Media_Platform.repository.UserRepository;
import com.example.Social_Media_Platform.service.AuthService;
import com.example.Social_Media_Platform.service.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ModelMapper modelMapper;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository,
                              UserRepository userRepository, AuthService authService, ModelMapper modelMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    @Override
    public CommentDto createComment(String token, String postId, String content) {
        User user = authService.getCurrentUser(token);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> PostNotFoundException.withId(postId));

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(user.getId());
        comment.setContent(content);
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());

        Comment savedComment = commentRepository.save(comment);
        postRepository.incrementCommentCount(postId);

        return mapToCommentDto(savedComment, user);
    }

     @Override
    public PaginationResponse getCommentsByPostId(String postId, int limit, String lastEvaluatedKey) {

        List<Comment>list=commentRepository.findByPostId(postId,limit,lastEvaluatedKey);
        boolean hasMore=!(list.size()<limit);
        lastEvaluatedKey = hasMore ? list.get(list.size() - 1).getId() : null;
        return new PaginationResponse(list,lastEvaluatedKey,limit,hasMore);
    }

    @Override
    public void deleteComment(String token, String commentId) {
        User user = authService.getCurrentUser(token);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new PostNotFoundException("Comment not found"));

        if (!comment.getUserId().equals(user.getId())) {
            throw new PostNotFoundException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
        postRepository.decrementCommentCount(comment.getPostId());
    }

    @Override
    public CommentDto mapToCommentDto(Comment comment, User user) {
        CommentDto commentDto = modelMapper.map(comment, CommentDto.class);
        modelMapper.map(user, commentDto);
        return commentDto;
    }
}