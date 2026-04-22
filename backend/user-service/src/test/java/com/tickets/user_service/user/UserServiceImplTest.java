package com.tickets.user_service.user;

import com.tickets.user_service.exception.UserAlreadyExistsException;
import com.tickets.user_service.exception.UserNotFoundException;
import com.tickets.user_service.user.application.CreateUserUseCase;
import com.tickets.user_service.user.application.DeleteUserUseCase;
import com.tickets.user_service.user.application.GetUserByIdUseCase;
import com.tickets.user_service.user.application.UpdateUserUseCase;
import com.tickets.user_service.user.application.dto.CreateUserCommand;
import com.tickets.user_service.user.application.dto.UpdateUserCommand;
import com.tickets.user_service.user.domain.User;
import com.tickets.user_service.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("User UseCases")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UUID userId;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        existingUser = User.create(
                userId, "Juan", "Pérez", "juan@test.com",
                "123456789", LocalDate.of(1990, 5, 20), "https://avatar.url");
    }

    // ── CreateUserUseCase ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("CreateUserUseCase")
    class CreateUserTests {

        private CreateUserUseCase useCase;

        @BeforeEach
        void init() { useCase = new CreateUserUseCase(userRepository); }

        @Test
        @DisplayName("debe crear el usuario cuando el email no existe")
        void shouldCreateUser_whenEmailDoesNotExist() {
            CreateUserCommand command = new CreateUserCommand(
                    userId, "Juan", "Pérez", "juan@test.com",
                    "123456789", LocalDate.of(1990, 5, 20), null);

            given(userRepository.existsByEmail("juan@test.com")).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(existingUser);

            User result = useCase.execute(command);

            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getEmail()).isEqualTo("juan@test.com");
            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("debe lanzar UserAlreadyExistsException cuando el email ya existe")
        void shouldThrow_whenEmailAlreadyExists() {
            CreateUserCommand command = new CreateUserCommand(
                    userId, "Juan", "Pérez", "juan@test.com", null, null, null);

            given(userRepository.existsByEmail("juan@test.com")).willReturn(true);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("juan@test.com");

            then(userRepository).should(never()).save(any());
        }
    }

    // ── GetUserByIdUseCase ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GetUserByIdUseCase")
    class GetUserByIdTests {

        private GetUserByIdUseCase useCase;

        @BeforeEach
        void init() { useCase = new GetUserByIdUseCase(userRepository); }

        @Test
        @DisplayName("debe retornar el usuario cuando existe")
        void shouldReturnUser_whenFound() {
            given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

            User result = useCase.execute(userId);

            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getFirstName()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("debe lanzar UserNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            UUID unknownId = UUID.randomUUID();
            given(userRepository.findById(unknownId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(unknownId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
        }
    }

    // ── UpdateUserUseCase ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("UpdateUserUseCase")
    class UpdateUserTests {

        private UpdateUserUseCase useCase;

        @BeforeEach
        void init() { useCase = new UpdateUserUseCase(userRepository); }

        @Test
        @DisplayName("debe actualizar solo los campos presentes en el comando")
        void shouldUpdateOnlyPresentFields() {
            UpdateUserCommand command = new UpdateUserCommand(
                    userId, "Carlos", null, "999888777", null, null);

            given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
            given(userRepository.save(any(User.class))).willReturn(existingUser);

            useCase.execute(command);

            assertThat(existingUser.getFirstName()).isEqualTo("Carlos");
            assertThat(existingUser.getPhone()).isEqualTo("999888777");
            assertThat(existingUser.getLastName()).isEqualTo("Pérez"); // sin cambio
        }

        @Test
        @DisplayName("debe lanzar UserNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            UUID unknownId = UUID.randomUUID();
            UpdateUserCommand command = new UpdateUserCommand(
                    unknownId, null, null, null, null, null);

            given(userRepository.findById(unknownId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should(never()).save(any());
        }
    }

    // ── DeleteUserUseCase ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("DeleteUserUseCase")
    class DeleteUserTests {

        private DeleteUserUseCase useCase;

        @BeforeEach
        void init() { useCase = new DeleteUserUseCase(userRepository); }

        @Test
        @DisplayName("debe eliminar el usuario cuando existe")
        void shouldDelete_whenUserExists() {
            given(userRepository.existsById(userId)).willReturn(true);

            useCase.execute(userId);

            then(userRepository).should().deleteById(userId);
        }

        @Test
        @DisplayName("debe lanzar UserNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            UUID unknownId = UUID.randomUUID();
            given(userRepository.existsById(unknownId)).willReturn(false);

            assertThatThrownBy(() -> useCase.execute(unknownId))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should(never()).deleteById(any());
        }
    }
}
