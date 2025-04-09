package com.example.Social_Media_Platform.controller;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.Util.MessageUtil;
import com.example.Social_Media_Platform.Util.ResponseHandler;
import com.example.Social_Media_Platform.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;
    private final MessageUtil messageUtil;

    public FriendController(FriendService friendService, MessageUtil messageUtil) {
        this.friendService = friendService;
        this.messageUtil = messageUtil;
    }

    @PostMapping("/requests")
    public ResponseEntity<ResponseHandler> sendFriendRequest(@RequestHeader("Authorization") String token,
                                                             @RequestParam String receiverId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseHandler(
                            friendService.sendFriendRequest(token, receiverId),
                            messageUtil.getMessage("friend.request.send.success"),
                            HttpStatus.CREATED.value(),
                            true,
                            "friend_request"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("friend.request.send.error"),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @PutMapping("/requests/{requestId}")
    public ResponseEntity<ResponseHandler> respondToFriendRequest(@RequestHeader("Authorization") String token,
                                                                  @PathVariable String requestId,
                                                                  @RequestParam String status) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            friendService.respondToFriendRequest(token, requestId, status),
                            messageUtil.getMessage("friend.request.respond.success"),
                            HttpStatus.OK.value(),
                            true,
                            "friend_request"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("friend.request.respond.error"),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<ResponseHandler> getFriendRequests(@RequestHeader("Authorization") String token,
                                                             @RequestParam(required = false) String status,
                                                             @RequestParam(required = false, defaultValue = "10") int limit,
                                                             @RequestParam(required = false) String lastEvaluatedKey) {
        try {
            Map<String, AttributeValue> lastKey = null;
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            friendService.getFriendRequests(token, status, limit, lastKey),
                            messageUtil.getMessage("friend.request.list.success"),
                            HttpStatus.OK.value(),
                            true,
                            "friend_requests"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("friend.request.list.error"),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @GetMapping
    public ResponseEntity<ResponseHandler> getFriends(@RequestHeader("Authorization") String token,
                                                      @RequestParam(required = false, defaultValue = "10") int limit,
                                                      @RequestParam(required = false) String lastEvaluatedKey) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            friendService.getFriends(token, limit, lastEvaluatedKey),
                            messageUtil.getMessage("friend.list.success"),
                            HttpStatus.OK.value(),
                            true,
                            "friends"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("friend.list.error"),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<ResponseHandler> removeFriend(@RequestHeader("Authorization") String token,
                                                        @PathVariable String friendId) {
        try {
            friendService.removeFriend(token, friendId);
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("friend.remove.success"),
                            HttpStatus.OK.value(),
                            true,
                            null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseHandler(
                            null,
                            messageUtil.getMessage("friend.remove.error"),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            false,
                            "error"));
        }
    }
}