package com.example.airreservation.model.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationAdminEditDTO {

    @NotNull
    private Long id;


    private String passengerFullName;
    private String flightInfo;

    @NotNull(message = "Numer miejsca nie może być pusty.")
    @Min(value = 1, message = "Numer miejsca musi być dodatni.")
    private Integer seatNumber;


}