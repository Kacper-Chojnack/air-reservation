package com.example.airreservation.service;

import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.passenger.PassengerDTO;
import com.example.airreservation.model.passenger.PassengerMapper;
import com.example.airreservation.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    @Transactional
    public PassengerDTO savePassenger(PassengerDTO passengerDTO) {

        Passenger passenger = passengerMapper.PassengerDTOToPassenger(passengerDTO);

        passenger.setPassword(passwordEncoder.encode(passengerDTO.getPassword()));

        passenger.setRole("ROLE_USER");

        Passenger savedPassenger = passengerRepository.save(passenger);
        return passengerMapper.PassengerToPassengerDTO(savedPassenger);
    }

    public boolean existsByEmail(String email) {
        return passengerRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return passengerRepository.existsByPhoneNumber(phoneNumber);
    }

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }
}