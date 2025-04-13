package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmailAlreadyExistsException extends ResponseStatusException {
    public EmailAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "Wprowadzony email jest już zajęty.");
    }
}
