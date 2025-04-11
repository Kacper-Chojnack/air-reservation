package com.example.airreservation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Reservation {

    @Id
    private Long id;
    private long reservationNumber;
    private long flightNumber;
    private int seatNumber;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean isDeparted;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

}
