package com.tickets.auth_service.credential.application;

import com.tickets.auth_service.credential.domain.RefreshTokenRepository;
import com.tickets.auth_service.shared.UseCase;

@UseCase
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void execute(String refreshToken) {
        refreshTokenRepository.revokeByToken(refreshToken);
    }
}
