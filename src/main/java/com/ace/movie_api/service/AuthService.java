package com.ace.movie_api.service;

import com.ace.movie_api.auth.entities.ForgotPassword;
import com.ace.movie_api.auth.entities.RefreshToken;
import com.ace.movie_api.auth.entities.UserEntity;
import com.ace.movie_api.auth.entities.UserRole;
import com.ace.movie_api.auth.repositories.ForgotPasswordRepository;
import com.ace.movie_api.auth.repositories.UserRepository;
import com.ace.movie_api.auth.service.JwtService;
import com.ace.movie_api.auth.service.RefreshTokenService;
import com.ace.movie_api.auth.utils.AuthResponse;
import com.ace.movie_api.auth.utils.LoginRequest;
import com.ace.movie_api.auth.utils.RefreshTokenRequest;
import com.ace.movie_api.auth.utils.RegisterRequest;
import com.ace.movie_api.dto.ForgotPasswordDto;
import com.ace.movie_api.dto.MailBody;
import com.ace.movie_api.dto.ResetPasswordDto;
import com.ace.movie_api.exceptions.DuplicateEmailException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final ForgotPasswordRepository forgotPasswordRepository;

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
           UserEntity user = findUser(request.getEmail());
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

    @Transactional
    public String forgotPassword(ForgotPasswordDto dto) {
        UserEntity user = userRepository.findByEmail(dto.email()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Integer otp = generateOtp();
        MailBody mailBody = MailBody.builder()
                .to(dto.email())
                .subject("Forgot Password Request")
                .text("Use this OTP to reset your password request: "+ otp)
                .build();
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .otp(otp)
                .user(user)
                .expirationTime(new Date(System.currentTimeMillis() +70 * 1000))
                .build();
        forgotPasswordRepository.save(forgotPassword);

        emailService.sendSimpleMessage(mailBody);
        return "Check your email for OTP token";
    }

    private Integer generateOtp(){
        Random random = new Random();

        return random.nextInt(100_000, 999_999);
    }

    private UserEntity findUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public String resetPassword(ResetPasswordDto dto) {
        try{
            UserEntity user = findUser(dto.email());
            ForgotPassword forgotPassword = forgotPasswordRepository.findByOtpAndUser(dto.otp(), user)
                    .orElseThrow(() -> new RuntimeException("Invalid OTP And EMail provide"));
            if(forgotPassword.getExpirationTime().before(new Date())) {
                forgotPasswordRepository.delete(forgotPassword);
//                throw new RuntimeException("OTP has expired, Please request a new password");
                return  "OTP has expired, Please request a new password";
            }
//        UserEntity data = UserEntity.builder()
//                .id(user.getId())
//                .email(user.getEmail())
//                .username(user.getUsername())
//                .role(user.getRole())
//                .password(passwordEncoder.encode(dto.password()))
//                .build();
            userRepository.updatePassword(user.getId(), passwordEncoder.encode(dto.password()));

            return "Password Reset successfully, Please login";
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}
