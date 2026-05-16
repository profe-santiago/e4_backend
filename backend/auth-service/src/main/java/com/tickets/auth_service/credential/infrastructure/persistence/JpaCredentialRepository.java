package com.tickets.auth_service.credential.infrastructure.persistence;

import com.tickets.auth_service.credential.domain.Credential;
import com.tickets.auth_service.credential.domain.CredentialRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCredentialRepository implements CredentialRepository {

    private final SpringDataCredentialRepository springData;
    private final CredentialPersistenceMapper mapper;

    public JpaCredentialRepository(SpringDataCredentialRepository springData,
                                    CredentialPersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

    @Override
    public Optional<Credential> findById(Long id) {
        return springData.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Credential> findByEmail(String email) {
        return springData.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springData.existsByEmail(email);
    }

    @Override
    public Credential save(Credential credential) {
        return mapper.toDomain(springData.save(mapper.toJpaEntity(credential)));
    }
}
