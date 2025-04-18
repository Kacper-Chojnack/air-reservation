package com.example.airreservation.repository;

import com.example.airreservation.model.airport.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {

}
