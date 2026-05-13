package com.tickets.auth_service.credential.application;

import com.tickets.auth_service.credential.application.dto.AuthResult;
import com.tickets.auth_service.credential.application.dto.LoginCommand;
import com.tickets.auth_service.credential.domain.Credential;
import com.tickets.auth_service.credential.domain.CredentialRepository;
import com.tickets.auth_service.credential.domain.PasswordHasher;
import com.tickets.auth_service.credential.domain.RefreshToken;
import com.tickets.auth_service.credential.domain.RefreshTokenRepository;
import com.tickets.auth_service.credential.domain.TokenService;
import com.tickets.auth_service.exception.InvalidCredentialsException;
import com.tickets.auth_service.shared.UseCase;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso: autenticación con email y contraseña.
 * Nunca revela si el email existe o no — siempre lanza InvalidCredentialsException
 * para prevenir enumeración de usuarios.
 */
@UseCase
public class LoginUseCase {

    private final CredentialRepository credentialRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginUseCase(CredentialRepository credentialRepository,
                        PasswordHasher passwordHasher,
                        TokenService tokenService,
                        RefreshTokenRepository refreshTokenRepository) {
        this.credentialRepository = credentialRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthResult execute(LoginCommand command) {
        Credential credential = credentialRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!credential.isActive()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordHasher.matches(command.password(), credential.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenService.generate(
                credential.getUserId(), credential.getEmail(), credential.getRole());

        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.create(
                credential.getId(), refreshTokenValue, LocalDateTime.now().plusDays(7)));

        return new AuthResult(accessToken, refreshTokenValue, credential.getRole(), credential.getEmail());
    }
}
