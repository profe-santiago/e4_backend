package com.tickets.user_service.user.application;

import com.tickets.user_service.exception.UserNotFoundException;
import com.tickets.user_service.shared.UseCase;
import com.tickets.user_service.user.domain.UserRepository;

import java.util.UUID;

@UseCase
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }
}
