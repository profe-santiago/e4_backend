package com.tickets.auth_service.credential.infrastructure.rest;

import com.tickets.auth_service.credential.application.dto.AuthResult;
import com.tickets.auth_service.credential.application.dto.LoginCommand;
import com.tickets.auth_service.credential.application.dto.RegisterCommand;
import com.tickets.auth_service.credential.infrastructure.rest.dto.AuthResponse;
import com.tickets.auth_service.credential.infrastructure.rest.dto.LoginRequest;
import com.tickets.auth_service.credential.infrastructure.rest.dto.RegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthRestMapper {

    public RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(request.getEmail(), request.getPassword());
    }

    public LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.getEmail(), request.getPassword());
    }

    public AuthResponse toResponse(AuthResult result) {
        return AuthResponse.builder()
                .token(result.token())
                .refreshToken(result.refreshToken())
                .role(result.role())
                .email(result.email())
                .build();
    }
}
