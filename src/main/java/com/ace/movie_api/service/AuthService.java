package com.ace.movie_api.service;

import com.ace.movie_api.auth.entities.RefreshToken;
import com.ace.movie_api.auth.entities.UserEntity;
import com.ace.movie_api.auth.entities.UserRole;
import com.ace.movie_api.auth.repositories.UserRepository;
import com.ace.movie_api.auth.service.JwtService;
import com.ace.movie_api.auth.service.RefreshTokenService;
import com.ace.movie_api.auth.utils.AuthResponse;
import com.ace.movie_api.auth.utils.LoginRequest;
import com.ace.movie_api.auth.utils.RefreshTokenRequest;
import com.ace.movie_api.auth.utils.RegisterRequest;
import com.ace.movie_api.exceptions.DuplicateEmailException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        try{
            UserEntity userData = UserEntity.builder()
                    .name(registerRequest.getName())
                    .email(registerRequest.getEmail())
                    .username(registerRequest.getUsername())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .role(UserRole.USER)
                    .build();
            var userExist = userRepository.findByEmail(registerRequest.getEmail());

            if(userExist.isPresent()){
                throw new DuplicateEmailException("User already registered");
            }

            UserEntity user = userRepository.save(userData);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(registerRequest.getEmail());
            return issueTokens(user, refreshToken);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    private AuthResponse issueTokens(UserEntity user, RefreshToken refreshToken) {
        String accessToken = jwtService.generateToken(user);


        return AuthResponse.builder()
                .accessToken(accessToken)
                .user(user)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
       try{
           authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
           );
           UserEntity user = userRepository.findByEmail(request.getEmail())
                   .orElseThrow(() -> new UsernameNotFoundException("User not found"));
           RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
           return issueTokens(user, refreshToken);
       }catch (Exception e) {
           System.out.println(e);
           throw new RuntimeException(e);
       }
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        UserEntity user = refreshToken.getUser();
        return issueTokens(user, refreshToken);
    }
}
