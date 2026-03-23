package com.tickets.auth_service.credentials;

import com.tickets.auth_service.credentials.dto.AuthResponse;
import com.tickets.auth_service.credentials.dto.LoginRequest;
import com.tickets.auth_service.credentials.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class CredentialController {

    private final CredentialService credentialService;

    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return credentialService.register(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return credentialService.login(req);
    }
}