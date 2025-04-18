package com.example.airreservation.repository;

import com.example.airreservation.model.lock.TemporarySeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TemporarySeatLockRepository extends JpaRepository<TemporarySeatLock, Long> {


    Optional<TemporarySeatLock> findByPassengerIdAndFlightIdAndSeatNumber(Long passengerId, Long flightId, Integer seatNumber);


    @Modifying
    @Query("DELETE FROM TemporarySeatLock tsl WHERE tsl.expiresAt < :now")
    int deleteExpiredLocks(@Param("now") LocalDateTime now);


    @Query("SELECT tsl.seatNumber FROM TemporarySeatLock tsl WHERE tsl.flight.id = :flightId AND tsl.expiresAt > :now")
    List<Integer> findLockedSeatsByFlightId(@Param("flightId") Long flightId, @Param("now") LocalDateTime now);

    Optional<TemporarySeatLock> findByFlightIdAndSeatNumber(Long flightId, Integer seatNumber);

    @Modifying
    void deleteByFlightId(Long flightId);

    boolean existsByFlightIdAndSeatNumberAndExpiresAtAfter(Long flightId, Integer seatNumber, LocalDateTime now);

}