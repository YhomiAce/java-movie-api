package com.ace.movie_api.auth.utils;

import com.ace.movie_api.auth.entities.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;

    private String refreshToken;

    private UserEntity user;
}
