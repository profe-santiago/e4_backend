package com.tickets.auth_service.credential.infrastructure.rest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String role;
    private String email;
}
