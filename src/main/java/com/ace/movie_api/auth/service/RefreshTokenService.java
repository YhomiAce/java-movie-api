package com.ace.movie_api.auth.service;

import com.ace.movie_api.auth.entities.RefreshToken;
import com.ace.movie_api.auth.entities.UserEntity;
import com.ace.movie_api.auth.repositories.RefreshTokenRepository;
import com.ace.movie_api.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    //    @Value("${jwt.refresh-token-key}")
//    private String secretKey;
//
//    @Value("${jwt.refresh-token-expiration-time}")
//    private long expirationTime;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(String username) {
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        RefreshToken refreshToken = user.getRefreshToken();
        if (refreshToken == null) {
            long refreshTokenValidity = 5 * 60 * 60 * 10000;
            refreshToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .expirationTime(Instant.now().plusMillis(refreshTokenValidity))
                    .user(user)
                    .build();

            return refreshTokenRepository.save(refreshToken);
        }

        return refreshToken;
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Refresh Token Not Found"));
        if(refreshToken.getExpirationTime().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh Token Expired");
        }
        return refreshToken;
    }
}
