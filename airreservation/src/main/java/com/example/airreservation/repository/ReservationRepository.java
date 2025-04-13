package com.example.airreservation.repository;

import com.example.airreservation.model.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // Znajduje wszystkie rezerwacje dla danego lotu
    List<Reservation> findByFlightId(Long flightId);

    // Wersja z JOIN FETCH dla optymalizacji (jeśli potrzebne są dane pasażera)
    @Query("SELECT r FROM Reservation r " +
            "LEFT JOIN FETCH r.passenger " +
            "WHERE r.flight.id = :flightId")
    List<Reservation> findByFlightIdWithPassengers(@Param("flightId") Long flightId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.flight.id = :flightId AND r.seatNumber = :seat")
    boolean existsByFlightIdAndSeatNumber(
            @Param("flightId") Long flightId,
            @Param("seat") Integer seat
    );

    @Query("SELECT r FROM Reservation r WHERE r.flight.id = :flightId ORDER BY r.createdAt DESC")
    List<Reservation> findReservationHistoryByFlightId(@Param("flightId") Long flightId);

    @Query("SELECT r FROM Reservation r WHERE r.passenger.id = :passengerId")
    List<Reservation> findReservationHistoryByPassengerId(@Param("passengerId") Long passengerId);

    @Query("SELECT r.seatNumber FROM Reservation r " +
            "WHERE r.flight.id = :flightId AND r.isActive = true")
    List<Integer> findActiveSeatsByFlightId(@Param("flightId") Long flightId);

}
