package com.example.airreservation.repository;

import com.example.airreservation.model.passenger.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Passenger> findByEmail(String email);

    Optional<Passenger> findByConfirmationToken(String confirmationToken);
}
