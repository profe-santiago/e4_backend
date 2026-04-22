package com.tickets.ticket_service.shared;

import com.tickets.ticket_service.exception.UnauthorizedActionException;
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

    public static void verifyOwnerOrAdmin(UUID resourceOwnerId, Authentication auth) {
        if (!resourceOwnerId.equals(getUserId(auth)) && !isAdmin(auth)) {
            throw new UnauthorizedActionException(
                    "No tenés permisos para acceder a este recurso");
        }
    }
}
