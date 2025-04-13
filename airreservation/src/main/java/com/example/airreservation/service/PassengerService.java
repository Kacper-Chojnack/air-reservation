package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.EmailAlreadyExistsException;
import com.example.airreservation.exceptionHandler.PhoneNumberAlreadyExistsException;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.passenger.PassengerDTO;
import com.example.airreservation.model.passenger.PassengerMapper;
import com.example.airreservation.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PassengerService {
    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;

    public PassengerService(PassengerRepository passengerRepository, PassengerMapper passengerMapper) {
        this.passengerRepository = passengerRepository;
        this.passengerMapper = passengerMapper;
    }

    @Transactional
    public PassengerDTO savePassenger(PassengerDTO passengerDTO) {
        Passenger passenger = passengerMapper.PassengerDTOToPassenger(passengerDTO);

        if (passengerRepository.existsByEmail(passenger.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        if (passengerRepository.existsByPhoneNumber(passenger.getPhoneNumber())){
            throw new PhoneNumberAlreadyExistsException();
        }

        Passenger savedPassenger = passengerRepository.save(passenger);
        return passengerMapper.PassengerToPassengerDTO(savedPassenger);
    }

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }
}