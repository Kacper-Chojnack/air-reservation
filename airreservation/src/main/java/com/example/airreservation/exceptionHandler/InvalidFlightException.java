package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidFlightException extends ResponseStatusException {
    public InvalidFlightException() {
        super(HttpStatus.NOT_FOUND, "Lotniska muszą się różnić.");
    }
}