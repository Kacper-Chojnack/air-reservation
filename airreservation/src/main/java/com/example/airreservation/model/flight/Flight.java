package com.example.airreservation.model.flight;

import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
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
    private boolean isRoundTrip;

    private boolean completed;

    @ToString.Exclude
    @OneToMany(mappedBy = "flight")
    private List<Reservation> reservations;

    @JoinColumn(name = "airplane_id", referencedColumnName = "id")
    @ManyToOne
    private Airplane airplane;

    private LocalDateTime departureDate;

    public boolean isDeparted() {
        return LocalDateTime.now().isAfter(departureDate);
    }

    public boolean isCompleted() {
        return LocalDateTime.now().isAfter(departureDate.plus(flightDuration));
    }

    @Getter
    @Transient
    private String formattedDuration;

}
