package com.tickets.auth_service.credentials.dto;

public class AuthResponse {

    private String token;
    private String role;
    private String email;

    public AuthResponse(String token, String role, String email) {
        this.token = token;
        this.role = role;
        this.email = email;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
}