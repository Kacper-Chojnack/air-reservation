package com.example.airreservation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationDTO {

    //@NotNull(message = "Numer rezerwacji nie może być pusty.")
    private String reservationNumber;
   // @NotNull(message = "Numer lotu nie może być pusty.")
    private String flightNumber;
    @NotNull(message = "Miejsce nie może być puste.")
    private int seatNumber;
    //@NotBlank(message = "Imię i nazwisko nie mogą być puste.")
    private String fullName;
   // @NotBlank(message = "Email nie może być pusty.")
    private String email;
    //@NotBlank(message = "Numer telefonu nie może być pusty.")
    private String phoneNumber;
    //@NotNull(message = "Informacja czy lot się odbył nie może być pusta.")
    private boolean departed;
    @NotNull(message = "Lot nie może być pusty.")
    private long flightId;
    @NotNull(message = "Pasażer nie może być pusty.")
    private long passengerId;


}
