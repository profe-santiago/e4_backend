package com.tickets.auth_service.credential.infrastructure.rest;

import com.tickets.auth_service.credential.application.LoginUseCase;
import com.tickets.auth_service.credential.application.RegisterUseCase;
import com.tickets.auth_service.credential.infrastructure.rest.dto.AuthResponse;
import com.tickets.auth_service.credential.infrastructure.rest.dto.LoginRequest;
import com.tickets.auth_service.credential.infrastructure.rest.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class AuthController {

    private final RegisterUseCase register;
    private final LoginUseCase login;
    private final AuthRestMapper mapper;

    public AuthController(RegisterUseCase register,
                           LoginUseCase login,
                           AuthRestMapper mapper) {
        this.register = register;
        this.login = login;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevas credenciales")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return mapper.toResponse(register.execute(mapper.toCommand(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return mapper.toResponse(login.execute(mapper.toCommand(request)));
    }
}
