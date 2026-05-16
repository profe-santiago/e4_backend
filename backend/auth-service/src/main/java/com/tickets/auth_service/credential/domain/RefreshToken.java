package com.tickets.auth_service.credential.domain;

import java.time.LocalDateTime;

public class RefreshToken {

    private Long id;
    private Long credentialId;
    private String token;
    private LocalDateTime expiresAt;
    private boolean revoked;
    private LocalDateTime createdAt;

    public RefreshToken() {}

    public static RefreshToken create(Long credentialId, String token, LocalDateTime expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.credentialId = credentialId;
        rt.token = token;
        rt.expiresAt = expiresAt;
        rt.revoked = false;
        rt.createdAt = LocalDateTime.now();
        return rt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public Long getId() { return id; }
    public Long getCredentialId() { return credentialId; }
    public String getToken() { return token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setCredentialId(Long credentialId) { this.credentialId = credentialId; }
    public void setToken(String token) { this.token = token; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
