package com.example.airreservation.repository;

import com.example.airreservation.model.flight.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByDepartureDateBeforeAndCompletedFalse(LocalDateTime departureTime);
}