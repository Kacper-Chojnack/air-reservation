package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AirplaneNotFoundException extends ResponseStatusException {
    public AirplaneNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Nie znaleziono samolotu.");
    }
}