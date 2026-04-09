package com.docuquery.backend.service;

import com.docuquery.backend.dto.request.LoginRequest;
import com.docuquery.backend.dto.request.RegisterRequest;
import com.docuquery.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}