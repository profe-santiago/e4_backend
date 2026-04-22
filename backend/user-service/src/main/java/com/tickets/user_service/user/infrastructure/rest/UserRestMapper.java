package com.tickets.user_service.user.infrastructure.rest;

import com.tickets.user_service.user.application.dto.CreateUserCommand;
import com.tickets.user_service.user.application.dto.UpdateUserCommand;
import com.tickets.user_service.user.domain.User;
import com.tickets.user_service.user.infrastructure.rest.dto.CreateUserRequest;
import com.tickets.user_service.user.infrastructure.rest.dto.UpdateUserRequest;
import com.tickets.user_service.user.infrastructure.rest.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserRestMapper {

    public CreateUserCommand toCommand(CreateUserRequest request, UUID userId) {
        return new CreateUserCommand(
                userId,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getBirthDate(),
                request.getAvatarUrl()
        );
    }

    public UpdateUserCommand toCommand(UpdateUserRequest request, UUID userId) {
        return new UpdateUserCommand(
                userId,
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getBirthDate(),
                request.getAvatarUrl()
        );
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
