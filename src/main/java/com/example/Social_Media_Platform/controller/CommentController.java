package com.example.Social_Media_Platform.controller;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.Util.MessageUtil;
import com.example.Social_Media_Platform.Util.ResponseHandler;
import com.example.Social_Media_Platform.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final MessageUtil messageUtil;

    public CommentController(CommentService commentService, MessageUtil messageUtil) {
        this.commentService = commentService;
        this.messageUtil = messageUtil;
    }

    @PostMapping
    public ResponseEntity<ResponseHandler> createComment(@RequestHeader("Authorization") String token,
                                                         @RequestParam String postId,
                                                         @RequestParam String content) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseHandler(
                            commentService.createComment(token, postId, content),
                            messageUtil.getMessage("comment.create.success"),
                            HttpStatus.CREATED.value(),
                            true,
                            "comment"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("comment.create.error"),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<ResponseHandler> getPostComments(@PathVariable String postId,
                                                           @RequestParam(required = false, defaultValue = "10") int limit,
                                                           @RequestParam(required = false) String lastEvaluatedKey) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            commentService.getCommentsByPostId(postId, limit, lastEvaluatedKey),
                            messageUtil.getMessage("comment.list.success"),
                            HttpStatus.OK.value(),
                            true,
                            "comments"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            /*messageUtil.getMessage("comment.list.error"),*/
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseHandler> deleteComment(@RequestHeader("Authorization") String token,
                                                         @PathVariable String commentId) {
        try {
            commentService.deleteComment(token, commentId);
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("comment.delete.success"),
                            HttpStatus.OK.value(),
                            true,
                            null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("comment.delete.error"),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }
}