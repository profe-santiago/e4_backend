package com.tickets.user_service.user.application;

import com.tickets.user_service.exception.UserNotFoundException;
import com.tickets.user_service.shared.UseCase;
import com.tickets.user_service.user.domain.User;
import com.tickets.user_service.user.domain.UserRepository;

import java.util.UUID;

@UseCase
public class GetUserByIdUseCase {

    private final UserRepository userRepository;

    public GetUserByIdUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
