package com.tickets.user_service.user;

import com.tickets.user_service.user.dto.CreateUserRequest;
import com.tickets.user_service.user.dto.UpdateUserRequest;
import com.tickets.user_service.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear perfil de usuario (el userId se extrae del JWT)")
    public UserResponse create(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return userService.createUser(userId, request);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil propio")
    public UserResponse getMe(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return userService.findById(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil por ID")
    public UserResponse getById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil propio")
    public UserResponse update(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar perfil propio")
    public void delete(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        userService.deleteUser(userId);
    }
}
