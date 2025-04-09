package com.example.Social_Media_Platform.dtos;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendshipDto {
    private String userId;
    @NotBlank(message = "FriendId is required")
    private String friendId;
    @NotBlank(message = "Status is required")
    private String status;
}
