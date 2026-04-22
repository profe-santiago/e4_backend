package com.tickets.user_service.shared;

import org.springframework.security.core.Authentication;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID getUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }

    public static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
