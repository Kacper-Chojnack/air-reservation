package com.example.airreservation.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reservationNumber;
    private String flightNumber;
    private int seatNumber;
    private String fullName;
    private String email;
    private String phoneNumber;

    private boolean departed;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

}
