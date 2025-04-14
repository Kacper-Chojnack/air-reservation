package com.example.airreservation.model.schedule;

import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RecurringFlightSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departure_airport_id", nullable = false)
    private Airport departureAirport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arrival_airport_id", nullable = false)
    private Airport arrivalAirport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "airplane_id", nullable = false)
    private Airplane airplane;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private Duration flightDuration;

    @Column(nullable = false)
    private String flightNumberPrefix;

    @Column(nullable = false)
    private int generateMonthsAhead = 3;

    @Column(nullable = false)
    private boolean active = true;
}