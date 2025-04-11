package com.example.airreservation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Flight {
    @Id
    private Long id;
    private String departureAirport;
    private String arrivalAirport;
    private int flightDuration;
    private long flightNumber;
    private boolean isRoundTrip;
    private int seatNumber;

    @OneToMany(mappedBy = "flight")
    private List<Reservation> reservations;
}
