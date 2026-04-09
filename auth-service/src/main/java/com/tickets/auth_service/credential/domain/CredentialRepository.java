package com.tickets.auth_service.credential.domain;

import java.util.Optional;

/**
 * Puerto secundario (salida) del bounded context Credential.
 * Interface pura — sin Spring, sin JPA.
 */
public interface CredentialRepository {

    Optional<Credential> findByEmail(String email);

    boolean existsByEmail(String email);

    Credential save(Credential credential);
}
