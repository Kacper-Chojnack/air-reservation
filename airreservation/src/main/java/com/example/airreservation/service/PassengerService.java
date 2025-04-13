package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.EmailAlreadyExistsException;
import com.example.airreservation.model.*;
import com.example.airreservation.repository.PassengerRepository;
import org.springframework.stereotype.Service;

@Service
public class PassengerService {
    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;

    public PassengerService(PassengerRepository passengerRepository, PassengerMapper passengerMapper) {
        this.passengerRepository = passengerRepository;
        this.passengerMapper = passengerMapper;
    }

    public PassengerDTO savePassenger(PassengerDTO passengerDTO) {
        Passenger passenger = passengerMapper.PassengerDTOToPassenger(passengerDTO);

        if (passengerRepository.existsByEmail(passenger.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        Passenger savedPassenger = passengerRepository.save(passenger);
        return passengerMapper.PassengerToPassengerDTO(savedPassenger);
    }
}