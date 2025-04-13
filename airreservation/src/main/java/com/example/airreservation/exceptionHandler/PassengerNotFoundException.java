package com.example.airreservation.exceptionHandler;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PassengerNotFoundException extends ResponseStatusException {
    public PassengerNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Nie znaleziono pasażera.");
    }
}