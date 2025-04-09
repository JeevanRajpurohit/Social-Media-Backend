package com.example.Social_Media_Platform.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class FriendResponseDto {
    private String id;

    @NotBlank(message = "UserId is required")
    private String userId;
    @NotBlank(message = "FriendId is required")
    private String friendId;
    private Date createdAt;
    private String friendUsername;
    private String friendAvatarUrl;
}
