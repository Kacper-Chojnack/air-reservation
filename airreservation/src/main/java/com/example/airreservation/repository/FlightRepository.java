package com.example.airreservation.repository;

import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByDepartureDateBeforeAndCompletedFalse(LocalDateTime departureTime);

    @Query("SELECT f FROM Flight f " +
            "JOIN FETCH f.departureAirport da " +
            "JOIN FETCH f.arrivalAirport aa " +
            "WHERE f.departureAirport.id = :departureAirportId " +

            "AND (:arrivalAirportId IS NULL OR f.arrivalAirport.id = :arrivalAirportId) " +

            "AND f.departureDate >= :startOfDay AND f.departureDate <= :endOfDay " +


            "ORDER BY f.departureDate ASC")
    Page<Flight> findFlightsByCriteria(
            @Param("departureAirportId") Long departureAirportId,
            @Param("arrivalAirportId") Long arrivalAirportId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,

            Pageable pageable);


    @Query("SELECT f FROM Flight f JOIN FETCH f.departureAirport JOIN FETCH f.arrivalAirport " +
            "WHERE f.departureDate > :currentTime " +
            "AND f.completed = false " +
            "ORDER BY f.departureDate ASC")
    Page<Flight> findUpcomingAvailable(
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable
    );

    boolean existsByDepartureAirportAndArrivalAirportAndDepartureDate(
            Airport departureAirport,
            Airport arrivalAirport,
            LocalDateTime departureDate
    );
}