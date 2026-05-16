package com.tickets.user_service.user.infrastructure.rest;

import com.tickets.user_service.user.application.GetUserByIdUseCase;
import com.tickets.user_service.user.infrastructure.rest.dto.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
class InternalUserController {

    private final GetUserByIdUseCase getUserById;
    private final UserRestMapper mapper;

    InternalUserController(GetUserByIdUseCase getUserById, UserRestMapper mapper) {
        this.getUserById = getUserById;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(getUserById.execute(id));
    }
}
