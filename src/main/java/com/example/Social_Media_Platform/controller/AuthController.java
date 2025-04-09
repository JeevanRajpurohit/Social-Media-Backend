package com.example.Social_Media_Platform.controller;

import com.example.Social_Media_Platform.Util.MessageUtil;
import com.example.Social_Media_Platform.Util.ResponseHandler;
import com.example.Social_Media_Platform.Validations.ValidationGroups;
import com.example.Social_Media_Platform.dtos.LoginDto;
import com.example.Social_Media_Platform.dtos.TokenDto;
import com.example.Social_Media_Platform.dtos.UserDto;
import com.example.Social_Media_Platform.model.User;
import com.example.Social_Media_Platform.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final MessageUtil messageUtil;

    public AuthController(AuthService authService, MessageUtil messageUtil) {
        this.authService = authService;
        this.messageUtil = messageUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseHandler> register(@Validated({ValidationGroups.OnRegister.class, Default.class})@Valid @RequestBody UserDto registerDto) {
        try {
            User userResponse = authService.register(registerDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseHandler(
                            userResponse,
                            messageUtil.getMessage("user.register.success"),
                            HttpStatus.CREATED.value(),
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

    @PostMapping("/login")
    public ResponseEntity<ResponseHandler> login(@Valid @RequestBody LoginDto loginDto) {
        try {
            TokenDto tokens = authService.login(loginDto);
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            tokens,
                            messageUtil.getMessage("user.login.success"),
                            HttpStatus.OK.value(),
                            true,
                            "tokens"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseHandler(
                            null,
                            e.getMessage(),
                            HttpStatus.UNAUTHORIZED.value(),
                            false,
                            "error"));
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<ResponseHandler> refreshToken(@RequestBody TokenDto tokenDto) {
        try {
            TokenDto newTokens = authService.refreshToken(tokenDto.getRefreshToken());
            return ResponseEntity.ok()
                    .body(new ResponseHandler(
                            newTokens,
                            messageUtil.getMessage("token.refresh.success"),
                            HttpStatus.OK.value(),
                            true,
                            "tokens"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseHandler(
                            null,
                            e.getMessage(),
                            HttpStatus.UNAUTHORIZED.value(),
                            false,
                            "error"));
        }
    }
}