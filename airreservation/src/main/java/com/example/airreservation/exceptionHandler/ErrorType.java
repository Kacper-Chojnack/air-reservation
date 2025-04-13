package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    FLIGHT_NOT_FOUND(HttpStatus.NOT_FOUND, "Lot nie został znaleziony"),
    SEAT_OCCUPIED(HttpStatus.CONFLICT, "Miejsce %d jest już zajęte"),
    PASSENGER_NOT_FOUND(HttpStatus.NOT_FOUND, "Pasażer nie został znaleziony"),
    FLIGHT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "Lot jest niedostępny (odwołany lub zakończony)"),
    INCORRECT_SEAT(HttpStatus.BAD_REQUEST, "Nieprawidłowy numer miejsca (dostępne: 1-%d)"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Rezerwacja o id %d nie istnieje"),
    AIRPORT_SAME_ERROR(HttpStatus.BAD_REQUEST, "Lotniska muszą się różnić");

    private final HttpStatus status;
    private final String message;

    ErrorType(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public BusinessException create(Object... args) {
        return new BusinessException(status, String.format(message, args));
    }
}