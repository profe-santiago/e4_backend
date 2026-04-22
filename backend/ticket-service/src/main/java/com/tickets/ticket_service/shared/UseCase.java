package com.tickets.ticket_service.shared;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * Anotación semántica que identifica un caso de uso de aplicación.
 * Combina @Service para que Spring lo detecte, pero expresa la intención de negocio.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface UseCase {
}
