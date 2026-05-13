package com.tickets.auth_service.credential.infrastructure.persistence;

import com.tickets.auth_service.credential.domain.RefreshToken;
import com.tickets.auth_service.credential.domain.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springData;

    public JpaRefreshTokenRepository(SpringDataRefreshTokenRepository springData) {
        this.springData = springData;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = toJpaEntity(refreshToken);
        return toDomain(springData.save(entity));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springData.findByToken(token).map(this::toDomain);
    }

    @Override
    @Transactional
    public void revokeByToken(String token) {
        springData.revokeByToken(token);
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        RefreshToken rt = new RefreshToken();
        rt.setId(entity.getId());
        rt.setCredentialId(entity.getCredentialId());
        rt.setToken(entity.getToken());
        rt.setExpiresAt(entity.getExpiresAt());
        rt.setRevoked(entity.isRevoked());
        rt.setCreatedAt(entity.getCreatedAt());
        return rt;
    }

    private RefreshTokenJpaEntity toJpaEntity(RefreshToken domain) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(domain.getId());
        entity.setCredentialId(domain.getCredentialId());
        entity.setToken(domain.getToken());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setRevoked(domain.isRevoked());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
