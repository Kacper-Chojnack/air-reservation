package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FlightNotFoundException extends ResponseStatusException {
    public FlightNotFoundException() {
        super(HttpStatus.CONFLICT, "Lot nie został znaleziony.");
    }

}