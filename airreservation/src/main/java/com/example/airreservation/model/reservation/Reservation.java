package com.example.airreservation.model.reservation;

import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.passenger.Passenger;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reservationNumber;
    private String flightNumber;
    private Integer seatNumber;
    private String fullName;
    private String email;
    private String phoneNumber;

    private boolean departed;

    private LocalDateTime createdAt;
    private boolean isActive;


    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "passenger_id", referencedColumnName = "id")
    private Passenger passenger;
}
