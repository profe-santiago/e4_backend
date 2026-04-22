package com.tickets.auth_service.credential.infrastructure.persistence;

import com.tickets.auth_service.credential.domain.Credential;
import org.springframework.stereotype.Component;

@Component
public class CredentialPersistenceMapper {

    public Credential toDomain(CredentialJpaEntity entity) {
        Credential c = new Credential();
        c.setId(entity.getId());
        c.setUserId(entity.getUserId());
        c.setEmail(entity.getEmail());
        c.setPasswordHash(entity.getPasswordHash());
        c.setRole(entity.getRole());
        c.setActive(entity.isActive());
        c.setCreatedAt(entity.getCreatedAt());
        return c;
    }

    public CredentialJpaEntity toJpaEntity(Credential domain) {
        CredentialJpaEntity entity = new CredentialJpaEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setRole(domain.getRole());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
