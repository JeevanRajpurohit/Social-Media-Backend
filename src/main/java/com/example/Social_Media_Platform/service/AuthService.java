package com.example.Social_Media_Platform.service;

import com.example.Social_Media_Platform.dtos.LoginDto;
import com.example.Social_Media_Platform.dtos.TokenDto;
import com.example.Social_Media_Platform.dtos.UserDto;
import com.example.Social_Media_Platform.model.User;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {

    @Transactional
    User register(UserDto registerDto);

    @Transactional
    TokenDto login(LoginDto loginDto);

    @Transactional
    TokenDto refreshToken(String refreshToken);

    User getCurrentUser(String token);

    void saveUserToken(User user, String jwtToken, String tokenType);
}
