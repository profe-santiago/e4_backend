package com.tickets.user_service.user;

import com.tickets.user_service.exception.UserAlreadyExistsException;
import com.tickets.user_service.exception.UserNotFoundException;
import com.tickets.user_service.user.dto.CreateUserRequest;
import com.tickets.user_service.user.dto.UpdateUserRequest;
import com.tickets.user_service.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserResponse createUser(UUID userId, CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setId(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setBirthDate(request.getBirthDate());
        user.setAvatarUrl(request.getAvatarUrl());

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)  user.setLastName(request.getLastName());
        if (request.getPhone() != null)     user.setPhone(request.getPhone());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }
}
