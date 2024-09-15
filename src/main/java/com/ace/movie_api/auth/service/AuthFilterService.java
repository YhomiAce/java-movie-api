package com.ace.movie_api.auth.service;

import com.ace.movie_api.auth.entities.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Service
public class AuthFilterService extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public AuthFilterService(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try{
            final String authHeader = request.getHeader("Authorization");

            // if no authorization header
            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                filterChain.doFilter(request, response);
                return;
            }

            // get the jwt token
            String jwtToken = authHeader.substring(7);
//        System.out.println(jwtToken);
            // extract username from jwt in the sub or subject of jwt claim
            String username = jwtService.extractUsername(jwtToken);

            // check if username return is not null
            // check if user is not logged in: that is check whether there is a user data in the authentication middleware
            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if(jwtService.isTokenValid(jwtToken, userDetails)){
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    System.out.println("AUTHORITIES: "+ userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    System.out.println("LOGGIN USER: "+ currentUser.getName() + "\nROLE: "+ currentUser.getRole());
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println(e);
//            throw new RuntimeException(e);
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

    }
}
