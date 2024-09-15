package com.ace.movie_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ForgotPasswordDto(
        @NotEmpty(message = "Email is required")
        @Email(message = "Provide a valid email")
        String email
) {
}
