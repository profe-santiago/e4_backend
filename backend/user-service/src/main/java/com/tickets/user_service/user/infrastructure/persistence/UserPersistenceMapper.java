package com.tickets.user_service.user.infrastructure.persistence;

import com.tickets.user_service.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public User toDomain(UserJpaEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setFirstName(entity.getFirstName());
        user.setLastName(entity.getLastName());
        user.setEmail(entity.getEmail());
        user.setPhone(entity.getPhone());
        user.setBirthDate(entity.getBirthDate());
        user.setAvatarUrl(entity.getAvatarUrl());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());
        return user;
    }

    public UserJpaEntity toJpaEntity(User domain) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(domain.getId());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
        entity.setBirthDate(domain.getBirthDate());
        entity.setAvatarUrl(domain.getAvatarUrl());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
