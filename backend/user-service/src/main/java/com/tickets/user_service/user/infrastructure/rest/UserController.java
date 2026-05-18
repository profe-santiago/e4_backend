package com.tickets.user_service.user.infrastructure.rest;

import com.tickets.user_service.exception.UnauthorizedActionException;
import com.tickets.user_service.shared.SecurityUtils;
import com.tickets.user_service.user.application.CreateUserUseCase;
import com.tickets.user_service.user.application.DeleteUserUseCase;
import com.tickets.user_service.user.application.GetUserByIdUseCase;
import com.tickets.user_service.user.application.UpdateUserUseCase;
import com.tickets.user_service.user.infrastructure.rest.dto.CreateUserRequest;
import com.tickets.user_service.user.infrastructure.rest.dto.UpdateUserRequest;
import com.tickets.user_service.user.infrastructure.rest.dto.UserResponse;
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

    private final CreateUserUseCase createUser;
    private final GetUserByIdUseCase getUserById;
    private final UpdateUserUseCase updateUser;
    private final DeleteUserUseCase deleteUser;
    private final UserRestMapper mapper;

    public UserController(CreateUserUseCase createUser,
                           GetUserByIdUseCase getUserById,
                           UpdateUserUseCase updateUser,
                           DeleteUserUseCase deleteUser,
                           UserRestMapper mapper) {
        this.createUser = createUser;
        this.getUserById = getUserById;
        this.updateUser = updateUser;
        this.deleteUser = deleteUser;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear perfil de usuario (el userId se extrae del JWT)")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request,
                                Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        return mapper.toResponse(createUser.execute(mapper.toCommand(request, userId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil propio")
    public UserResponse getMe(Authentication auth) {
        return mapper.toResponse(getUserById.execute(SecurityUtils.getUserId(auth)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil por ID")
    public UserResponse getById(@PathVariable UUID id, Authentication auth) {
        UUID requesterId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        if (!isAdmin && !id.equals(requesterId)) {
            throw new UnauthorizedActionException("No tenés permisos para ver este perfil");
        }
        return mapper.toResponse(getUserById.execute(id));
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil propio")
    public UserResponse update(@Valid @RequestBody UpdateUserRequest request,
                                Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        return mapper.toResponse(updateUser.execute(mapper.toCommand(request, userId)));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar perfil propio")
    public void delete(Authentication auth) {
        deleteUser.execute(SecurityUtils.getUserId(auth));
    }
}
