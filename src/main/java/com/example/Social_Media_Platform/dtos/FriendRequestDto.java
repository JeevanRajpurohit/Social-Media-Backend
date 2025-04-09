package com.example.Social_Media_Platform.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class FriendRequestDto {
    private String id;
    @NotBlank(message = "SenderId is required")
    private String senderId;

    @NotBlank(message = "ReceiverId is required")
    private String receiverId;

    @NotBlank(message = "Status is required")
    private String status;
    private Date createdAt;
    private String senderUsername;
    private String senderAvatarUrl;
    private String receiverUsername;
    private String receiverAvatarUrl;
}
