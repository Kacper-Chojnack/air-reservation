package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AirportNotFoundException extends ResponseStatusException {
    public AirportNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Nie znaleziono lotniska.");
    }
}
