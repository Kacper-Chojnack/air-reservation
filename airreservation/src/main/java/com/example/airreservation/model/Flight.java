package com.example.airreservation.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "departure_airport_id", referencedColumnName = "id")
    private Airport departureAirport;
    @OneToOne
    @JoinColumn(name = "arrival_airport_id", referencedColumnName = "id")
    private Airport arrivalAirport;
    private Duration flightDuration;
    private String flightNumber;
    private boolean isRoundTrip;

    private List<Integer> occupiedSeatsNumbers = new ArrayList<>();

    @OneToMany(mappedBy = "flight")
    private List<Reservation> reservations = new ArrayList<>();

    @JoinColumn(name = "airplane_id", referencedColumnName = "id")
    @ManyToOne
    private Airplane airplane;

    private LocalDateTime departureDate;
}
