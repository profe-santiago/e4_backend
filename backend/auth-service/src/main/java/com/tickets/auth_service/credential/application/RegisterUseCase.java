package com.tickets.auth_service.credential.application;

import com.tickets.auth_service.credential.application.dto.AuthResult;
import com.tickets.auth_service.credential.application.dto.RegisterCommand;
import com.tickets.auth_service.credential.domain.Credential;
import com.tickets.auth_service.credential.domain.CredentialRepository;
import com.tickets.auth_service.credential.domain.PasswordHasher;
import com.tickets.auth_service.credential.domain.TokenService;
import com.tickets.auth_service.exception.EmailAlreadyRegisteredException;
import com.tickets.auth_service.shared.UseCase;

/**
 * Caso de uso: registro de nuevas credenciales.
 * SRP: solo orquesta — delega hashing al puerto PasswordHasher y token al puerto TokenService.
 */
@UseCase
public class RegisterUseCase {

    private final CredentialRepository credentialRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public RegisterUseCase(CredentialRepository credentialRepository,
                            PasswordHasher passwordHasher,
                            TokenService tokenService) {
        this.credentialRepository = credentialRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    public AuthResult execute(RegisterCommand command) {
        if (credentialRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException(command.email());
        }

        String hash = passwordHasher.hash(command.password());
        Credential credential = Credential.create(command.email(), hash);
        Credential saved = credentialRepository.save(credential);

        String token = tokenService.generate(saved.getUserId(), saved.getEmail(), saved.getRole());
        return new AuthResult(token, saved.getRole(), saved.getEmail());
    }
}
