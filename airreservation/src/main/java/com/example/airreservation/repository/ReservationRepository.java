package com.example.airreservation.repository;

import com.example.airreservation.model.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByFlightId(Long flightId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.flight.id = :flightId AND r.seatNumber = :seat")
    boolean existsByFlightIdAndSeatNumber(
            @Param("flightId") Long flightId,
            @Param("seat") Integer seat
    );

    List<Reservation> findByPassengerIdOrderByFlightDepartureDateDesc(Long passengerId);


}
