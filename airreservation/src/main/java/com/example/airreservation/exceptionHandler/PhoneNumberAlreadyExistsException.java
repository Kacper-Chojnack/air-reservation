package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PhoneNumberAlreadyExistsException extends ResponseStatusException {
    public PhoneNumberAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "Pasażer z podanym numerem telefonu już istnieje.");
    }
}