package com.tickets.user_service.user.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario (salida) del bounded context User.
 * Interface pura — sin Spring, sin JPA.
 */
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsById(UUID id);

    User save(User user);

    void deleteById(UUID id);
}
