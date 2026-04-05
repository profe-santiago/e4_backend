package com.tickets.user_service.user;

import com.tickets.user_service.exception.UserAlreadyExistsException;
import com.tickets.user_service.exception.UserNotFoundException;
import com.tickets.user_service.user.dto.CreateUserRequest;
import com.tickets.user_service.user.dto.UpdateUserRequest;
import com.tickets.user_service.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        existingUser = new User();
        existingUser.setId(userId);
        existingUser.setFirstName("Juan");
        existingUser.setLastName("Pérez");
        existingUser.setEmail("juan@test.com");
        existingUser.setPhone("123456789");
        existingUser.setBirthDate(LocalDate.of(1990, 5, 20));
        existingUser.setAvatarUrl("https://avatar.url");
        // Simula @PrePersist
        try {
            var onCreate = User.class.getDeclaredMethod("onCreate");
            onCreate.setAccessible(true);
            onCreate.invoke(existingUser);
        } catch (Exception e) {
            existingUser.setCreatedAt(LocalDateTime.now());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("debe crear el usuario y retornar UserResponse cuando el email no existe")
        void shouldCreateUser_whenEmailDoesNotExist() {
            CreateUserRequest request = buildCreateRequest("juan@test.com");
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(existingUser);

            UserResponse response = userService.createUser(userId, request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(captor.capture());
            User saved = captor.getValue();

            assertThat(saved.getId()).isEqualTo(userId);
            assertThat(saved.getEmail()).isEqualTo("juan@test.com");
            assertThat(response.getId()).isEqualTo(userId);
            assertThat(response.getEmail()).isEqualTo("juan@test.com");
        }

        @Test
        @DisplayName("debe lanzar UserAlreadyExistsException cuando el email ya está registrado")
        void shouldThrow_whenEmailAlreadyExists() {
            CreateUserRequest request = buildCreateRequest("juan@test.com");
            given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

            assertThatThrownBy(() -> userService.createUser(userId, request))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("juan@test.com");

            then(userRepository).should(never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("debe retornar UserResponse cuando el usuario existe")
        void shouldReturnUser_whenFound() {
            given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

            UserResponse response = userService.findById(userId);

            assertThat(response.getId()).isEqualTo(userId);
            assertThat(response.getFirstName()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("debe lanzar UserNotFoundException cuando el usuario no existe")
        void shouldThrow_whenNotFound() {
            UUID unknownId = UUID.randomUUID();
            given(userRepository.findById(unknownId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(unknownId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByEmail
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("debe retornar UserResponse cuando el email existe")
        void shouldReturnUser_whenFound() {
            given(userRepository.findByEmail("juan@test.com")).willReturn(Optional.of(existingUser));

            UserResponse response = userService.findByEmail("juan@test.com");

            assertThat(response.getEmail()).isEqualTo("juan@test.com");
        }

        @Test
        @DisplayName("debe lanzar UserNotFoundException cuando el email no existe")
        void shouldThrow_whenNotFound() {
            given(userRepository.findByEmail("no@existe.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findByEmail("no@existe.com"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("no@existe.com");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("debe actualizar solo los campos presentes en el request")
        void shouldUpdateOnlyPresentFields() {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setFirstName("Carlos");
            request.setPhone("999888777");

            given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
            given(userRepository.save(any(User.class))).willReturn(existingUser);

            userService.updateUser(userId, request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(captor.capture());
            User updated = captor.getValue();

            assertThat(updated.getFirstName()).isEqualTo("Carlos");
            assertThat(updated.getPhone()).isEqualTo("999888777");
            // el apellido NO cambia porque no vino en el request
            assertThat(updated.getLastName()).isEqualTo("Pérez");
        }

        @Test
        @DisplayName("debe lanzar UserNotFoundException cuando el usuario no existe")
        void shouldThrow_whenNotFound() {
            UUID unknownId = UUID.randomUUID();
            given(userRepository.findById(unknownId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(unknownId, new UpdateUserRequest()))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should(never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("debe eliminar el usuario cuando existe")
        void shouldDelete_whenUserExists() {
            given(userRepository.existsById(userId)).willReturn(true);

            userService.deleteUser(userId);

            then(userRepository).should().deleteById(userId);
        }

        @Test
        @DisplayName("debe lanzar UserNotFoundException cuando el usuario no existe")
        void shouldThrow_whenNotFound() {
            UUID unknownId = UUID.randomUUID();
            given(userRepository.existsById(unknownId)).willReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(unknownId))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should(never()).deleteById(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // helpers
    // ─────────────────────────────────────────────────────────────────────────
    private CreateUserRequest buildCreateRequest(String email) {
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstName("Juan");
        req.setLastName("Pérez");
        req.setEmail(email);
        req.setPhone("123456789");
        req.setBirthDate(LocalDate.of(1990, 5, 20));
        return req;
    }
}
