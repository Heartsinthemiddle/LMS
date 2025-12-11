package com.lms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user login request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user login")
public class LoginRequest {

//    @NotBlank(message = "Username or email is required")
//    @Schema(description = "Username or email address", example = "john_doe", required = true)
    private String usernameOrEmail;

//    @NotBlank(message = "Password is required")
//    @Schema(description = "User password", example = "SecurePass123", required = true)
    private String password;
}

