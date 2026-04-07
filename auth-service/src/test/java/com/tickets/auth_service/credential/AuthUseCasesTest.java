package com.tickets.auth_service.credential;

import com.tickets.auth_service.credential.application.LoginUseCase;
import com.tickets.auth_service.credential.application.RegisterUseCase;
import com.tickets.auth_service.credential.application.dto.AuthResult;
import com.tickets.auth_service.credential.application.dto.LoginCommand;
import com.tickets.auth_service.credential.application.dto.RegisterCommand;
import com.tickets.auth_service.credential.domain.Credential;
import com.tickets.auth_service.credential.domain.CredentialRepository;
import com.tickets.auth_service.credential.domain.PasswordHasher;
import com.tickets.auth_service.credential.domain.TokenService;
import com.tickets.auth_service.exception.EmailAlreadyRegisteredException;
import com.tickets.auth_service.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth UseCases")
class AuthUseCasesTest {

    @Mock private CredentialRepository credentialRepository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private TokenService tokenService;

    private Credential savedCredential;

    @BeforeEach
    void setUp() {
        savedCredential = Credential.create("juan@test.com", "hashed_password");
    }

    // ── RegisterUseCase ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("RegisterUseCase")
    class RegisterTests {

        private RegisterUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new RegisterUseCase(credentialRepository, passwordHasher, tokenService);
        }

        @Test
        @DisplayName("debe registrar y retornar token cuando el email no existe")
        void shouldRegister_whenEmailDoesNotExist() {
            given(credentialRepository.existsByEmail("juan@test.com")).willReturn(false);
            given(passwordHasher.hash("password123")).willReturn("hashed");
            given(credentialRepository.save(any(Credential.class))).willReturn(savedCredential);
            given(tokenService.generate(any(), anyString(), anyString())).willReturn("jwt-token");

            AuthResult result = useCase.execute(new RegisterCommand("juan@test.com", "password123"));

            assertThat(result.token()).isEqualTo("jwt-token");
            assertThat(result.role()).isEqualTo("BUYER");
            assertThat(result.email()).isEqualTo("juan@test.com");
        }

        @Test
        @DisplayName("debe lanzar EmailAlreadyRegisteredException cuando el email ya existe")
        void shouldThrow_whenEmailAlreadyExists() {
            given(credentialRepository.existsByEmail("juan@test.com")).willReturn(true);

            assertThatThrownBy(() ->
                    useCase.execute(new RegisterCommand("juan@test.com", "password123")))
                    .isInstanceOf(EmailAlreadyRegisteredException.class)
                    .hasMessageContaining("juan@test.com");

            then(credentialRepository).should(never()).save(any());
            then(tokenService).should(never()).generate(any(), any(), any());
        }
    }

    // ── LoginUseCase ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("LoginUseCase")
    class LoginTests {

        private LoginUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new LoginUseCase(credentialRepository, passwordHasher, tokenService);
        }

        @Test
        @DisplayName("debe autenticar y retornar token con credenciales válidas")
        void shouldLogin_withValidCredentials() {
            given(credentialRepository.findByEmail("juan@test.com"))
                    .willReturn(Optional.of(savedCredential));
            given(passwordHasher.matches("password123", savedCredential.getPasswordHash()))
                    .willReturn(true);
            given(tokenService.generate(any(), anyString(), anyString())).willReturn("jwt-token");

            AuthResult result = useCase.execute(new LoginCommand("juan@test.com", "password123"));

            assertThat(result.token()).isEqualTo("jwt-token");
            assertThat(result.role()).isEqualTo("BUYER");
        }

        @Test
        @DisplayName("debe lanzar InvalidCredentialsException si el email no existe")
        void shouldThrow_whenEmailNotFound() {
            given(credentialRepository.findByEmail("no@existe.com")).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    useCase.execute(new LoginCommand("no@existe.com", "password123")))
                    .isInstanceOf(InvalidCredentialsException.class);

            then(tokenService).should(never()).generate(any(), any(), any());
        }

        @Test
        @DisplayName("debe lanzar InvalidCredentialsException si la contraseña no coincide")
        void shouldThrow_whenPasswordMismatch() {
            given(credentialRepository.findByEmail("juan@test.com"))
                    .willReturn(Optional.of(savedCredential));
            given(passwordHasher.matches("wrong", savedCredential.getPasswordHash()))
                    .willReturn(false);

            assertThatThrownBy(() ->
                    useCase.execute(new LoginCommand("juan@test.com", "wrong")))
                    .isInstanceOf(InvalidCredentialsException.class);

            then(tokenService).should(never()).generate(any(), any(), any());
        }
    }
}
