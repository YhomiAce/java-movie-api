package com.ace.movie_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ResetPasswordDto(
        @NotEmpty
        @Email
        String email,
        @NotEmpty
        Integer otp,
        @NotBlank
        @Size(min = 5)
        String password
) {
}
