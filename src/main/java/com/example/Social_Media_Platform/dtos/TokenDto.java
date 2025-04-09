package com.example.Social_Media_Platform.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class TokenDto {
    @NotBlank(message = "Access Token is required")
    private String accessToken;
    @NotBlank(message = "Refresh Token is required")
    private String refreshToken;
}
