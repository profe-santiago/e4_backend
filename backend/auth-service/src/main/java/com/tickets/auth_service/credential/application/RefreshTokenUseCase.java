package com.tickets.auth_service.credential.application;

import com.tickets.auth_service.credential.application.dto.AuthResult;
import com.tickets.auth_service.credential.domain.Credential;
import com.tickets.auth_service.credential.domain.CredentialRepository;
import com.tickets.auth_service.credential.domain.RefreshToken;
import com.tickets.auth_service.credential.domain.RefreshTokenRepository;
import com.tickets.auth_service.credential.domain.TokenService;
import com.tickets.auth_service.exception.InvalidRefreshTokenException;
import com.tickets.auth_service.shared.UseCase;

import java.time.LocalDateTime;
import java.util.UUID;

@UseCase
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final CredentialRepository credentialRepository;
    private final TokenService tokenService;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                               CredentialRepository credentialRepository,
                               TokenService tokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.credentialRepository = credentialRepository;
        this.tokenService = tokenService;
    }

    public AuthResult execute(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!refreshToken.isValid()) {
            throw new InvalidRefreshTokenException();
        }

        Credential credential = credentialRepository.findById(refreshToken.getCredentialId())
                .orElseThrow(InvalidRefreshTokenException::new);

        // Rotación: revocar el token usado y emitir uno nuevo
        refreshTokenRepository.revokeByToken(refreshTokenValue);

        String newAccessToken = tokenService.generate(
                credential.getUserId(), credential.getEmail(), credential.getRole());

        String newRefreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.create(
                credential.getId(), newRefreshTokenValue, LocalDateTime.now().plusDays(7)));

        return new AuthResult(newAccessToken, newRefreshTokenValue, credential.getRole(), credential.getEmail());
    }
}
