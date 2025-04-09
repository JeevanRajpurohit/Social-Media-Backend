package com.example.Social_Media_Platform.dtos;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private String id;
    @NotBlank(message = "UserId is required")
    private String userId;
    @NotBlank(message = "Message is required")
    private String content;
    @NotBlank(message = "Image Path is required")
    private String imageUrl;
    @NotBlank(message = "Like count is required")
    private int likeCount;
    @NotBlank(message = "Comment count is required")
    private int commentCount;
    private Date createdAt;
    private Date updatedAt;
    private String username;
    private String avatarUrl;
}
