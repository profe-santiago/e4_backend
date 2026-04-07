package com.tickets.auth_service.credential.domain;

/**
 * Puerto secundario (salida) para el hashing de contraseñas.
 * El dominio define el contrato — la infraestructura elige BCrypt, Argon2, etc.
 *
 * Nombrado PasswordHasher (no PasswordEncoder) para evitar colisión
 * con org.springframework.security.crypto.password.PasswordEncoder.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
