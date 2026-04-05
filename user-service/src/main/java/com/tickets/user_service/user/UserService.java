package com.tickets.user_service.user;

import com.tickets.user_service.user.dto.CreateUserRequest;
import com.tickets.user_service.user.dto.UpdateUserRequest;
import com.tickets.user_service.user.dto.UserResponse;

import java.util.UUID;

// DIP: el controller depende de esta abstracción, no de la implementación concreta
public interface UserService {

    UserResponse createUser(UUID userId, CreateUserRequest request);

    UserResponse findById(UUID id);

    UserResponse findByEmail(String email);

    UserResponse updateUser(UUID userId, UpdateUserRequest request);

    void deleteUser(UUID userId);
}
