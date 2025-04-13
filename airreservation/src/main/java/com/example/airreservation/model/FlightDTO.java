package com.example.airreservation.model;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class FlightDTO {

    @NotNull(message = "Miejsce wylotu nie może być puste.")
    private Long departureAirportId;
    @NotNull(message = "Miejsce przylotu nie może być puste.")
    private Long arrivalAirportId;
    @NotNull(message = "Długośc lotu nie może być pusta.")
    private Duration flightDuration;
    @NotBlank(message = "Numer lotu nie może być pusty.")
    private String flightNumber;
    @NotNull(message = "Wybór lotu powrotnego nie może być pusty.")
    private boolean isRoundTrip;
    @NotNull(message = "Numer miejsca przypisanego do rezerwacji nie może być pusty.")
    private int seatNumber;
    @NotNull(message = "Samolot musi zostać wybrany.")
    private Long airplane;
    @NotNull(message = "Data odlotu musi zostać wybrana.")
    private LocalDateTime departureDate;

}
