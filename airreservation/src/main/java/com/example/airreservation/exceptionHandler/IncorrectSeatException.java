package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class IncorrectSeatException extends ResponseStatusException {
    public IncorrectSeatException() {
        super(HttpStatus.BAD_REQUEST, "Wprowadzono nieprawidłowe miejsce.");
    }
}