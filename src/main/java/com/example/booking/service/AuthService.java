package com.example.booking.service;

import com.example.booking.dto.LoginRequest;
import com.example.booking.dto.LoginResponse;
import com.example.booking.entity.User;
import com.example.booking.exception.BadRequestException;
import com.example.booking.repository.UserRepository;
import com.example.booking.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        // This will throw BadCredentialsException if authentication fails
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(token, user.getEmail(), user.getRole().name());
    }
}
