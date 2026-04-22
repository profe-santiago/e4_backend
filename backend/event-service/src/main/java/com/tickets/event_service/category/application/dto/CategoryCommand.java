package com.tickets.event_service.category.application.dto;

/**
 * Command de entrada para crear o actualizar una categoría.
 * Record inmutable — el UseCase no toca objetos HTTP.
 */
public record CategoryCommand(String name, String description) {
}
