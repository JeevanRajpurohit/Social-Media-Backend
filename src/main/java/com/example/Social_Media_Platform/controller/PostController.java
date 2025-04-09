package com.example.Social_Media_Platform.controller;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.Util.MessageUtil;
import com.example.Social_Media_Platform.Util.ResponseHandler;
import com.example.Social_Media_Platform.dtos.PostDto;
import com.example.Social_Media_Platform.model.Post;
import com.example.Social_Media_Platform.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final MessageUtil messageUtil;

    public PostController(PostService postService, MessageUtil messageUtil) {
        this.postService = postService;
        this.messageUtil = messageUtil;
    }

    @PostMapping
    public ResponseEntity<ResponseHandler> createPost(@RequestHeader("Authorization") String token,
                                                      @RequestParam(required = false) String content,
                                                      @RequestParam(required = false) MultipartFile image) {
        try {
            PostDto postDto = postService.createPost(token, content, image);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseHandler(
                            postDto,
                            messageUtil.getMessage("post.create.success"),
                            HttpStatus.CREATED.value(),
                            true,
                            "post"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ResponseHandler> getPost(@PathVariable String postId) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            postService.getPostById(postId),
                            messageUtil.getMessage("post.get.success"),
                            HttpStatus.OK.value(),
                            true,
                            "post"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseHandler> getUserPosts(@PathVariable String userId,
                                                        @RequestParam(required = false, defaultValue = "10") int limit,
                                                        @RequestParam(required = false) String lastEvaluatedKey) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            postService.getPostsByUserId(userId, limit, lastEvaluatedKey),
                            messageUtil.getMessage("post.list.success"),
                            HttpStatus.OK.value(),
                            true,
                            "posts"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<ResponseHandler> getNewsFeed(@RequestHeader("Authorization") String token,
                                                       @RequestParam(defaultValue = "10") int limit) {
        try {
            List<PostDto> posts = postService.getNewsFeed(token, limit);

            return ResponseEntity.ok().body(new ResponseHandler(
                    posts,
                    messageUtil.getMessage("post.feed.success"),
                    HttpStatus.OK.value(),
                    true,
                    "posts"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"
                    ));
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ResponseHandler> deletePost(@RequestHeader("Authorization") String token,
                                                      @PathVariable String postId) {
        try {
            postService.deletePost(token, postId);
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("post.delete.success"),
                            HttpStatus.OK.value(),
                            true,
                            null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ResponseHandler> likePost(@RequestHeader("Authorization") String token,
                                                    @PathVariable String postId) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            postService.likePost(token, postId),
                            messageUtil.getMessage("post.like.success"),
                            HttpStatus.OK.value(),
                            true,
                            "post"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @PostMapping("/{postId}/unlike")
    public ResponseEntity<ResponseHandler> unlikePost(@RequestHeader("Authorization") String token,
                                                      @PathVariable String postId) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            postService.unlikePost(token, postId),
                            messageUtil.getMessage("post.unlike.success"),
                            HttpStatus.OK.value(),
                            true,
                            "post"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }
}