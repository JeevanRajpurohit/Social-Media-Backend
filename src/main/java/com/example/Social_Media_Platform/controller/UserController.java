package com.example.Social_Media_Platform.controller;

import com.example.Social_Media_Platform.Util.MessageUtil;
import com.example.Social_Media_Platform.Util.ResponseHandler;
import com.example.Social_Media_Platform.Validations.ValidationGroups;
import com.example.Social_Media_Platform.dtos.UserDto;
import com.example.Social_Media_Platform.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final MessageUtil messageUtil;

    public UserController(UserService userService, MessageUtil messageUtil) {
        this.userService = userService;
        this.messageUtil = messageUtil;
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseHandler> getCurrentUserProfile(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            userService.getCurrentUserProfile(token),
                            messageUtil.getMessage("user.profile.success"),
                            HttpStatus.OK.value(),
                            true,
                            "profile"));
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

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseHandler> getUserProfile(@PathVariable String userId) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            userService.getUserProfile(userId),
                            messageUtil.getMessage("user.profile.success"),
                            HttpStatus.OK.value(),
                            true,
                            "profile"));
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

    @PutMapping("/me")
    public ResponseEntity<ResponseHandler> updateProfile(@Validated({ValidationGroups.OnUpdate.class, Default.class})@RequestHeader("Authorization") String token,
                                                         @Valid @RequestBody UserDto updateDto) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            userService.updateUserProfile(token, updateDto),
                            messageUtil.getMessage("user.update.success"),
                            HttpStatus.OK.value(),
                            true,
                            "user"));
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

    @PostMapping("/me/avatar")
    public ResponseEntity<ResponseHandler> uploadAvatar(@RequestHeader("Authorization") String token,
                                                        @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            userService.updateUserAvatar(token, file),
                            messageUtil.getMessage("user.avatar.success"),
                            HttpStatus.OK.value(),
                            true,
                            "user"));
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
}