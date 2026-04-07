package com.tickets.auth_service.credential.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataCredentialRepository extends JpaRepository<CredentialJpaEntity, Long> {

    Optional<CredentialJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
