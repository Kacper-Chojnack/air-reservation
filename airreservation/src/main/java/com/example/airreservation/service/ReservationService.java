package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.*;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.reservation.ReservationDTO;
import com.example.airreservation.model.reservation.ReservationMapper;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;
    private final FlightService flightService;

    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper, PassengerRepository passengerRepository, FlightRepository flightRepository, FlightService flightService) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.passengerRepository = passengerRepository;
        this.flightRepository = flightRepository;
        this.flightService = flightService;
    }


        private String generateReservationNumber (Flight flight,int seatNumber){
            return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        }

        public List<Reservation> getAllReservations () {
            return reservationRepository.findAll();
        }

        public List<Integer> getAvailableSeats (Long flightId){
            Flight flight = flightRepository.findById(flightId).orElseThrow();
            List<Integer> reservedSeats = reservationRepository
                    .findActiveSeatsByFlightId(flightId);

            return IntStream.rangeClosed(1, flight.getAirplane().getTotalSeats())
                    .filter(seat -> !reservedSeats.contains(seat))
                    .boxed()
                    .toList();
        }

    @Transactional
    public ReservationDTO saveReservation(ReservationDTO reservationDTO) {
        Reservation reservation = reservationMapper.ReservationDTOTOReservation(reservationDTO);

        Passenger passenger = passengerRepository.findById(reservationDTO.getPassengerId())
                .orElseThrow(() -> ErrorType.PASSENGER_NOT_FOUND.create());

        Flight flight = flightRepository.findById(reservationDTO.getFlightId())
                .orElseThrow(() -> ErrorType.FLIGHT_NOT_FOUND.create());

        if (flight.isDeparted() || flight.isCompleted()) {
            throw ErrorType.FLIGHT_NOT_AVAILABLE.create();
        }

        reservation.setDeparted(false);
        reservation.setEmail(passenger.getEmail());
        reservation.setFlight(flight);
        reservation.setFullName(passenger.getName() + " " + passenger.getSurname());
        reservation.setPhoneNumber(passenger.getPhoneNumber());
        reservation.setReservationNumber(generateReservationNumber(flight, reservation.getSeatNumber()));
        reservation.setFlightNumber(flight.getFlightNumber());
        reservation.setPassenger(passenger);
        reservation.setCreatedAt(LocalDateTime.now());

        int totalSeats = flight.getAirplane().getTotalSeats();
        if (reservation.getSeatNumber() > totalSeats || reservation.getSeatNumber() < 1) {
            throw ErrorType.INCORRECT_SEAT.create(totalSeats);
        }

        if (reservationRepository.existsByFlightIdAndSeatNumber(
                reservationDTO.getFlightId(),
                reservationDTO.getSeatNumber()
        )) {
            throw ErrorType.SEAT_OCCUPIED.create(reservationDTO.getSeatNumber());
        }

        flightRepository.save(flight);
        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.ReservationToReservationDTO(savedReservation);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ErrorType.RESERVATION_NOT_FOUND.create(reservationId));
        reservation.setActive(false);
        reservationRepository.save(reservation);
    }

    }