package com.tickets.auth_service.credentials;

import com.tickets.auth_service.credentials.dto.AuthResponse;
import com.tickets.auth_service.credentials.dto.LoginRequest;
import com.tickets.auth_service.credentials.dto.RegisterRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public CredentialService(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public AuthResponse register(RegisterRequest req) {
        if (credentialRepository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El email ya está registrado");
        }

        Credential credential = new Credential(
                UUID.randomUUID(),
                req.getEmail(),
                passwordEncoder.encode(req.getPassword())
        );

        credentialRepository.save(credential);
        String token = generateToken(credential);
        return new AuthResponse(token, credential.getRole(), credential.getEmail());
    }

    public AuthResponse login(LoginRequest req) {
        Credential credential = credentialRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(req.getPassword(), credential.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = generateToken(credential);
        return new AuthResponse(token, credential.getRole(), credential.getEmail());
    }

    private String generateToken(Credential credential) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(credential.getUserId().toString())
                .claim("role", credential.getRole())
                .claim("email", credential.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }
}