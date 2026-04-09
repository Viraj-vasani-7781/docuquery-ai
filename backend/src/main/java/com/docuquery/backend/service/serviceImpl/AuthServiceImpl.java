package com.docuquery.backend.service.impl;

import com.docuquery.backend.dto.request.LoginRequest;
import com.docuquery.backend.dto.request.RegisterRequest;
import com.docuquery.backend.dto.response.AuthResponse;
import com.docuquery.backend.entity.User;
import com.docuquery.backend.exception.CustomException;
import com.docuquery.backend.repository.UserRepository;
import com.docuquery.backend.security.JwtUtil;
import com.docuquery.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ── Register ─────────────────────────────────────────

    @Override
    public AuthResponse register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw CustomException.conflict("Email already registered");
        }

        // Build and save new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // ── Login ─────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {

        // Authenticate — throws BadCredentialsException if wrong
        // GlobalExceptionHandler catches it automatically
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If we reach here, credentials are correct
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> CustomException.notFound("User not found"));

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}