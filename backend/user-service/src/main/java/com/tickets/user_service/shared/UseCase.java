package com.tickets.user_service.shared;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * Anotación semántica para identificar casos de uso de la capa de aplicación.
 * Alias de @Service — registrado como bean de Spring pero con vocabulario de dominio.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface UseCase {
}
