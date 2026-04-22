package com.tickets.auth_service.credential.infrastructure.security;

import com.tickets.auth_service.credential.domain.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador secundario: implementa el puerto PasswordHasher usando BCrypt.
 * Si en el futuro se quiere cambiar a Argon2, solo se cambia este adaptador.
 */
@Component
public class BcryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
