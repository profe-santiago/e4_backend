package com.tickets.auth_service.credential.infrastructure.rest;

import com.tickets.auth_service.credential.application.LoginUseCase;
import com.tickets.auth_service.credential.application.LogoutUseCase;
import com.tickets.auth_service.credential.application.RefreshTokenUseCase;
import com.tickets.auth_service.credential.application.RegisterUseCase;
import com.tickets.auth_service.credential.infrastructure.rest.dto.AuthResponse;
import com.tickets.auth_service.credential.infrastructure.rest.dto.LoginRequest;
import com.tickets.auth_service.credential.infrastructure.rest.dto.LogoutRequest;
import com.tickets.auth_service.credential.infrastructure.rest.dto.RefreshRequest;
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
    private final RefreshTokenUseCase refreshToken;
    private final LogoutUseCase logout;
    private final AuthRestMapper mapper;

    public AuthController(RegisterUseCase register,
                          LoginUseCase login,
                          RefreshTokenUseCase refreshToken,
                          LogoutUseCase logout,
                          AuthRestMapper mapper) {
        this.register = register;
        this.login = login;
        this.refreshToken = refreshToken;
        this.logout = logout;
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

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token usando refresh token")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return mapper.toResponse(refreshToken.execute(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cerrar sesión e invalidar refresh token")
    public void logout(@Valid @RequestBody LogoutRequest request) {
        logout.execute(request.getRefreshToken());
    }
}
