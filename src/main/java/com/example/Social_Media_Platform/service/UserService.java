package com.example.Social_Media_Platform.service;

import com.example.Social_Media_Platform.dtos.UserDto;
import com.example.Social_Media_Platform.dtos.UserProfileDto;
import com.example.Social_Media_Platform.model.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    UserProfileDto getCurrentUserProfile(String token);

    UserProfileDto getUserProfile(String userId);

    @Transactional
    User updateUserProfile(String token, UserDto updateDto);

    @Transactional
    User updateUserAvatar(String token, MultipartFile file) throws IOException;
}
