package com.example.airreservation.model.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationDTO {

    private String reservationNumber;
    private String flightNumber;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean departed;

    @NotNull(message = "Miejsce nie może być puste.")
    @Min(value = 1, message = "Numer miejsca musi być dodatni.")
    private Integer seatNumber;

    @NotNull(message = "Identyfikator lotu nie może być pusty.")
    private Long flightId;

    @NotNull(message = "Identyfikator pasażera nie może być pusty.")
    private Long passengerId;

}