package com.example.airreservation.model;

import jakarta.persistence.Id;

import java.util.List;

public class FlightDTO {
    @Id
    private Long id;
    private String departureAirport;
    private String arrivalAirport;
    private int flightDuration;
    private long flightNumber;
    private boolean isRoundTrip;
    private int seatNumber;
    private List<Reservation> reservations;

}
