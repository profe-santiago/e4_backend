package com.tickets.event_service.exception;

public class DuplicateNameException extends RuntimeException {
    public DuplicateNameException(String entity, String name) {
        super("Ya existe un/a " + entity + " con el nombre: " + name);
    }
}
