package com.example.airreservation.scheduler;

import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FlightStatusScheduler {

    private final FlightRepository flightRepository;
    private final ReservationRepository reservationRepository;

    public FlightStatusScheduler(FlightRepository flightRepository, ReservationRepository reservationRepository) {
        this.flightRepository = flightRepository;
        this.reservationRepository = reservationRepository;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void updateFlightStatuses() {
        List<Flight> activeFlights = flightRepository.findByDepartureDateBeforeAndCompletedFalse(LocalDateTime.now());

        activeFlights.forEach(flight -> {
            List<Reservation> reservations = reservationRepository.findByFlightId(flight.getId());
            reservations.forEach(res -> res.setDeparted(true));
            reservationRepository.saveAll(reservations);

            if (flight.isCompleted()) {
                flight.setCompleted(true);
                flightRepository.save(flight);
            }
        });
    }
}