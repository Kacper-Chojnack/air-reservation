package com.example.airreservation.model.flight;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightDTO {

    @NotNull(message = "Miejsce wylotu nie może być puste.")
    private Long departureAirportId;
    @NotNull(message = "Miejsce przylotu nie może być puste.")
    private Long arrivalAirportId;
    @NotBlank(message = "Numer lotu nie może być pusty.")
    private String flightNumber;
    @NotNull(message = "Wybór lotu powrotnego nie może być pusty.")
    private boolean isRoundTrip;

    @NotNull(message = "Samolot musi zostać wybrany.")
    private Long airplane;
    @NotNull(message = "Data odlotu musi zostać wybrana.")
    @Future(message = "Data wylotu musi być datą w przyszłości.")
    private LocalDateTime departureDate;

    @NotNull(message = "Godziny trwania lotu nie mogą być puste.")
    private Integer flightDurationHours;

    @NotNull(message = "Minuty trwania lotu nie mogą być puste.")
    private Integer flightDurationMinutes;


}
