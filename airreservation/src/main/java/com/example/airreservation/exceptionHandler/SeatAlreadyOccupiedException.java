package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SeatAlreadyOccupiedException extends ResponseStatusException {
    public SeatAlreadyOccupiedException(int seatNumber) {
        super(HttpStatus.NOT_FOUND, "Miejsce nr " +seatNumber + " jest już zajęte.");
    }
}

