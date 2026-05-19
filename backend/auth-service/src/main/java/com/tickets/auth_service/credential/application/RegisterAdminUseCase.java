package com.tickets.auth_service.credential.application;

import com.tickets.auth_service.credential.application.dto.AuthResult;
import com.tickets.auth_service.credential.application.dto.RegisterCommand;
import com.tickets.auth_service.credential.domain.Credential;
import com.tickets.auth_service.credential.domain.CredentialRepository;
import com.tickets.auth_service.credential.domain.PasswordHasher;
import com.tickets.auth_service.credential.domain.RefreshToken;
import com.tickets.auth_service.credential.domain.RefreshTokenRepository;
import com.tickets.auth_service.credential.domain.TokenService;
import com.tickets.auth_service.exception.EmailAlreadyRegisteredException;
import com.tickets.auth_service.shared.UseCase;

import java.time.LocalDateTime;
import java.util.UUID;

@UseCase
public class RegisterAdminUseCase {

    private final CredentialRepository credentialRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public RegisterAdminUseCase(CredentialRepository credentialRepository,
                                PasswordHasher passwordHasher,
                                TokenService tokenService,
                                RefreshTokenRepository refreshTokenRepository) {
        this.credentialRepository = credentialRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthResult execute(RegisterCommand command) {
        if (credentialRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException(command.email());
        }

        String hash = passwordHasher.hash(command.password());
        Credential credential = Credential.createAdmin(command.email(), hash);
        Credential saved = credentialRepository.save(credential);

        String accessToken = tokenService.generate(saved.getUserId(), saved.getEmail(), saved.getRole());

        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.create(
                saved.getId(), refreshTokenValue, LocalDateTime.now().plusDays(7)));

        return new AuthResult(accessToken, refreshTokenValue, saved.getRole(), saved.getEmail());
    }
}
