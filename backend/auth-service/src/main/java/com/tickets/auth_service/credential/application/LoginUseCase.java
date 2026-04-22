package com.tickets.auth_service.credential.application;

import com.tickets.auth_service.credential.application.dto.AuthResult;
import com.tickets.auth_service.credential.application.dto.LoginCommand;
import com.tickets.auth_service.credential.domain.Credential;
import com.tickets.auth_service.credential.domain.CredentialRepository;
import com.tickets.auth_service.credential.domain.PasswordHasher;
import com.tickets.auth_service.credential.domain.TokenService;
import com.tickets.auth_service.exception.InvalidCredentialsException;
import com.tickets.auth_service.shared.UseCase;

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

    public LoginUseCase(CredentialRepository credentialRepository,
                         PasswordHasher passwordHasher,
                         TokenService tokenService) {
        this.credentialRepository = credentialRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
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

        String token = tokenService.generate(
                credential.getUserId(), credential.getEmail(), credential.getRole());
        return new AuthResult(token, credential.getRole(), credential.getEmail());
    }
}
