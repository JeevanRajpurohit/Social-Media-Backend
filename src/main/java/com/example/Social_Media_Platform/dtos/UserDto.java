package com.example.Social_Media_Platform.dtos;

import com.example.Social_Media_Platform.Validations.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String userId;

    @NotBlank(groups = ValidationGroups.OnRegister.class, message = "Name is required")
    private String email;

    @NotBlank(groups = ValidationGroups.OnRegister.class, message = "Name is required")
    private String username;

    @Size(min = 6, message = "Password size would be greater than 6")
    @NotBlank(groups = ValidationGroups.OnRegister.class, message = "Name is required")
    private String password;

    private String avatarUrl;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    @NotBlank(groups = ValidationGroups.OnUpdate.class, message = "Name is required")
    private String bio;

    @NotBlank(message = "status is required")
    private String status;
}
