package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FlightNotAvailableException extends ResponseStatusException {
    public FlightNotAvailableException() {
        super(HttpStatus.CONFLICT, "Nie można rezerwować tego lotu.");
    }
}