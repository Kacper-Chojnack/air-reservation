package com.example.airreservation.model.flight;

import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"reservations", "airplane"})
@EqualsAndHashCode(exclude = {"reservations", "airplane", "departureAirport", "arrivalAirport"})
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "departure_airport_id", referencedColumnName = "id")
    private Airport departureAirport;

    @ManyToOne
    @JoinColumn(name = "arrival_airport_id", referencedColumnName = "id")
    private Airport arrivalAirport;

    private Duration flightDuration;
    private String flightNumber;
    private boolean roundTrip;

    @OneToMany(mappedBy = "flight")
    private List<Reservation> reservations;

    @JoinColumn(name = "airplane_id", referencedColumnName = "id")
    @ManyToOne
    private Airplane airplane;

    private LocalDateTime departureDate;

    public boolean isDeparted() {
        return departureDate != null && LocalDateTime.now().isAfter(departureDate);
    }

    public boolean isCompleted() {
        if (departureDate == null || flightDuration == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(departureDate.plus(flightDuration));
    }

    private boolean completed = false;

}
