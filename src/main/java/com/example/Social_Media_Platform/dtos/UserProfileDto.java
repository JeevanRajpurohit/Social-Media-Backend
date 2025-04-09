package com.example.Social_Media_Platform.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserProfileDto {
    private String id;
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Email is required")
    private String email;

    private String avatarUrl;
    private String bio;
    @NotBlank(message = "Status is required")
    private String status;
    @NotBlank(message = "Friend count is required")
    private int friendCount;
    @NotBlank(message = "Post count is required")
    private int postCount;
}
