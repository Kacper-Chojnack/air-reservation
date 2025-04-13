package com.example.airreservation.repository;

import com.example.airreservation.model.Passenger;
import com.example.airreservation.model.PassengerDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    boolean existsByEmail(String email);

}
