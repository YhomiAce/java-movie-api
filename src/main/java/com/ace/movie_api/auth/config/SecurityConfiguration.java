package com.ace.movie_api.auth.config;

import com.ace.movie_api.auth.service.AuthFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    private final AuthFilterService authFilterService;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       try{
           http
                   .csrf(AbstractHttpConfigurer::disable)
                   .authorizeHttpRequests(auth -> auth
                           .requestMatchers("/api/v1/auth/**")
                           .permitAll()
                           .anyRequest()
                           .authenticated()
                   )
                   .sessionManagement(session -> session
                           .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                   )
                   .authenticationProvider(authenticationProvider)
                   .addFilterBefore(authFilterService, UsernamePasswordAuthenticationFilter.class);

           return http.build();
       } catch (Exception e) {
           System.out.println(e);
           throw new RuntimeException(e);
       }
    }
}
