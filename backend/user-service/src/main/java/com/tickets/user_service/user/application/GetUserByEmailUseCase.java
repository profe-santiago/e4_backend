package com.tickets.user_service.user.application;

import com.tickets.user_service.exception.UserNotFoundException;
import com.tickets.user_service.shared.UseCase;
import com.tickets.user_service.user.domain.User;
import com.tickets.user_service.user.domain.UserRepository;

@UseCase
public class GetUserByEmailUseCase {

    private final UserRepository userRepository;

    public GetUserByEmailUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}
