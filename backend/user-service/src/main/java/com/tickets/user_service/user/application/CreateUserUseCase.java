package com.tickets.user_service.user.application;

import com.tickets.user_service.exception.UserAlreadyExistsException;
import com.tickets.user_service.shared.UseCase;
import com.tickets.user_service.user.application.dto.CreateUserCommand;
import com.tickets.user_service.user.domain.User;
import com.tickets.user_service.user.domain.UserRepository;

@UseCase
public class CreateUserUseCase {

    private final UserRepository userRepository;

    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }
        User user = User.create(
                command.userId(),
                command.firstName(),
                command.lastName(),
                command.email(),
                command.phone(),
                command.birthDate(),
                command.avatarUrl()
        );
        return userRepository.save(user);
    }
}
