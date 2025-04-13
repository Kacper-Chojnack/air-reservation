package com.example.airreservation.repository;

import com.example.airreservation.model.flight.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    // Znajduje loty które już wystartowały, ale jeszcze się nie zakończyły
    List<Flight> findByDepartureDateBeforeAndCompletedFalse(LocalDateTime departureTime);

    // Do wyświetlania na stronie głównej
    @Query("SELECT f FROM Flight f WHERE f.departureDate > :now OR (f.departureDate <= :now AND f.completed = false)")
    List<Flight> findVisibleFlights(@Param("now") LocalDateTime now);

}
