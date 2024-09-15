package com.ace.movie_api.auth.utils;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotEmpty
    private String name;
    @NotEmpty
    private String email;
    @NotEmpty
    private String username;
    @NotEmpty
    @Size(min = 4)
    private String password;
}
