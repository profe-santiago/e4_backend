package com.tickets.auth_service.credential.infrastructure.security;

import com.tickets.auth_service.credential.domain.TokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Adaptador secundario: implementa el puerto TokenService usando JWT (jjwt).
 * Si en el futuro se quiere cambiar a otro mecanismo de tokens, solo se cambia este adaptador.
 */
@Component
public class JwtTokenService implements TokenService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    public String generate(UUID userId, String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }
}
