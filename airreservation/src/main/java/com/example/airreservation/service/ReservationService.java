package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.FlightNotFoundException;
import com.example.airreservation.exceptionHandler.PassengerNotFoundException;
import com.example.airreservation.model.*;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper, PassengerRepository passengerRepository, FlightRepository flightRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.passengerRepository = passengerRepository;
        this.flightRepository = flightRepository;
    }

    public ReservationDTO saveReservation(ReservationDTO reservationDTO) {
        Reservation reservation = reservationMapper.ReservationDTOTOReservation(reservationDTO);

        Passenger passenger = passengerRepository.findById(reservationDTO.getPassengerId())
                .orElseThrow(() -> new PassengerNotFoundException());

        Flight flight = flightRepository.findById(reservationDTO.getPassengerId())
                .orElseThrow(() -> new FlightNotFoundException());

        reservation.setDeparted(false);
        reservation.setEmail(passenger.getEmail());
        reservation.setFlight(flight);
        reservation.setFullName(passenger.getName() + " " +passenger.getSurname());
        reservation.setEmail(passenger.getEmail());
        reservation.setPhoneNumber(passenger.getPhoneNumber());
        reservation.setReservationNumber(generateReservationNumber(flight, reservation.getSeatNumber()));
        reservation.setFlightNumber(flight.getFlightNumber());
        reservation.setPassenger(passenger);

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.ReservationToReservationDTO(savedReservation);
    }

    private String generateReservationNumber(Flight flight, int seatNumber) {
        String base = flight.getDepartureAirport().getName() +
                flight.getArrivalAirport().getName() +
                flight.getFlightNumber() +
                seatNumber +
                LocalDate.now();
        String hash = UUID.nameUUIDFromBytes(base.getBytes()).toString().substring(0, 8);
        return base.substring(0, Math.min(base.length(), 10)) + hash;
    }
}