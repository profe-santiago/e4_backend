package com.tickets.user_service.user.infrastructure.persistence;

import com.tickets.user_service.user.domain.User;
import com.tickets.user_service.user.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springData;
    private final UserPersistenceMapper mapper;

    public JpaUserRepository(SpringDataUserRepository springData,
                              UserPersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springData.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springData.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springData.existsByEmail(email);
    }

    @Override
    public boolean existsById(UUID id) {
        return springData.existsById(id);
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(springData.save(mapper.toJpaEntity(user)));
    }

    @Override
    public void deleteById(UUID id) {
        springData.deleteById(id);
    }
}
