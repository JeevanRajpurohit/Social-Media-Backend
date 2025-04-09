package com.example.Social_Media_Platform.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private String id;
    @NotBlank(message = "PostId is required")
    private String postId;
    @NotBlank(message = "UserId is required")
    private String userId;
    @NotBlank(message = "comment is required")
    private String content;

    private Date createdAt;
    private Date updatedAt;

    private String username;
    private String avatarUrl;
}
