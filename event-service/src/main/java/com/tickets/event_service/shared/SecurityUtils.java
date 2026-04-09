package com.tickets.event_service.shared;

import com.tickets.event_service.exception.UnauthorizedActionException;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Utilidades de seguridad compartidas entre todos los módulos.
 * Centraliza la extracción de claims del JWT para evitar duplicación.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID getUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }

    public static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Verifica que el usuario autenticado sea el dueño del recurso o un ADMIN.
     * Lanza UnauthorizedActionException si no cumple ninguna condición.
     */
    public static void verifyOwnerOrAdmin(UUID resourceOwnerId, Authentication auth) {
        UUID requesterId = getUserId(auth);
        if (!resourceOwnerId.equals(requesterId) && !isAdmin(auth)) {
            throw new UnauthorizedActionException(
                    "No tenés permisos para realizar esta acción sobre este recurso");
        }
    }
}
