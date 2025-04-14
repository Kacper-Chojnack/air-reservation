package com.example.airreservation.model.reservation;

import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.passenger.Passenger;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString(exclude = {"flight", "passenger"})
@EqualsAndHashCode(exclude = {"flight", "passenger"})
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_reservation_flight_seat",
                columnNames = {"flight_id", "seat_number"}
        )
})
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

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", referencedColumnName = "id")
    private Passenger passenger;
}