package com.tickets.user_service.user.application;

import com.tickets.user_service.exception.UserNotFoundException;
import com.tickets.user_service.shared.UseCase;
import com.tickets.user_service.user.application.dto.UpdateUserCommand;
import com.tickets.user_service.user.domain.User;
import com.tickets.user_service.user.domain.UserRepository;

@UseCase
public class UpdateUserUseCase {

    private final UserRepository userRepository;

    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(UpdateUserCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // La lógica de actualización parcial vive en el dominio
        user.update(
                command.firstName(),
                command.lastName(),
                command.phone(),
                command.birthDate(),
                command.avatarUrl()
        );

        return userRepository.save(user);
    }
}
