package com.example.airreservation.exceptionHandler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorType {
    FLIGHT_NOT_FOUND(HttpStatus.NOT_FOUND, "Lot nie został znaleziony"),
    SEAT_OCCUPIED(HttpStatus.CONFLICT, "Miejsce %d jest już zajęte"),
    PASSENGER_NOT_FOUND(HttpStatus.NOT_FOUND, "Pasażer nie został znaleziony"),
    FLIGHT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "Lot jest niedostępny (odwołany lub zakończony)"),
    INCORRECT_SEAT(HttpStatus.BAD_REQUEST, "Nieprawidłowy numer miejsca (dostępne: 1-%d)"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Rezerwacja o id %d nie istnieje"),
    AIRPORT_SAME_ERROR(HttpStatus.BAD_REQUEST, "Lotniska muszą się różnić"),
    AIRPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "Nie znaleziono takiego lotniska"),
    AIRPLANE_NOT_FOUND(HttpStatus.NOT_FOUND, "Nie znaleziono takiego samolotu"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Pasażer z takim e-mailem już istnieje"),
    PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Pasażer z takim numerem telefonu już istnieje"),
    SEAT_LOCKED_BY_ANOTHER_USER(HttpStatus.CONFLICT, "Wybrane miejsce (nr %d) jest właśnie rezerwowane przez innego użytkownika, spróbuj ponownie później"),
    SEAT_LOCKED_OR_RESERVED(HttpStatus.CONFLICT, "Wybrane miejsce (nr %d) zostało w międzyczasie zablokowane lub zarezerwowane, spróbuj ponownie"),
    LOCK_NOT_FOUND(HttpStatus.CONFLICT, "Nie znaleziono rezerwacji"),
    LOCK_EXPIRED(HttpStatus.CONFLICT, "Wybrana rezerwacja wygasła"),
    INVALID_PASSWORD_FOR_RESERVATION(HttpStatus.CONFLICT, "Wprowadzono nieprawidłowe hasło"),
    FLIGHT_HAS_RESERVATIONS(HttpStatus.CONFLICT, "Nie można usunąć lotu (ID: %d), ponieważ istnieją powiązane rezerwacje."),
    TOO_SHORT_PASSWORD(HttpStatus.BAD_REQUEST, "Nowe hasło musi mieć minimum 8 znaków");


    @Getter
    private final HttpStatus status;
    private final String message;

    ErrorType(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public BusinessException create(Object... args) {
        String formattedReason = String.format(message, args);
        return new BusinessException(this, formattedReason);
    }
}