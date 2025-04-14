package com.example.airreservation.model.lock;

import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.passenger.Passenger;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor

@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"flight_id", "seat_number"}, name = "uk_temp_lock_flight_seat")
})
public class TemporarySeatLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(nullable = false)
    private Integer seatNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public TemporarySeatLock(Flight flight, Integer seatNumber, Passenger passenger, LocalDateTime expiresAt) {
        this.flight = flight;
        this.seatNumber = seatNumber;
        this.passenger = passenger;
        this.expiresAt = expiresAt;
    }
}
