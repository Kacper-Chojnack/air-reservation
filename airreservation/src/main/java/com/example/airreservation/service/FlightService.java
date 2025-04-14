package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.model.flight.FlightMapper;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.repository.AirplaneRepository;
import com.example.airreservation.repository.AirportRepository;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class FlightService {
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final AirportRepository airportRepository;
    private final AirplaneRepository airplaneRepository;
    private final ReservationRepository reservationRepository;

    public FlightService(FlightRepository flightRepository, FlightMapper flightMapper, AirportRepository airportRepository, AirplaneRepository airplaneRepository, ReservationRepository reservationRepository) {
        this.flightRepository = flightRepository;
        this.flightMapper = flightMapper;
        this.airportRepository = airportRepository;
        this.airplaneRepository = airplaneRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public FlightDTO saveFlight(FlightDTO flightDTO) {
        Flight flight = flightMapper.flightDTOToFlight(flightDTO);

        Airport departureAirport = airportRepository.findById(flightDTO.getDepartureAirportId())
                .orElseThrow(ErrorType.AIRPORT_NOT_FOUND::create);

        Airport arrivalAirport = airportRepository.findById(flightDTO.getArrivalAirportId())
                .orElseThrow(ErrorType.AIRPORT_NOT_FOUND::create);

        if (flightDTO.getDepartureAirportId().equals(flightDTO.getArrivalAirportId())) {
            throw ErrorType.AIRPORT_SAME_ERROR.create();
        }

        Airplane airplane = airplaneRepository.findById(flightDTO.getAirplane())
                .orElseThrow(ErrorType.AIRPLANE_NOT_FOUND::create);

        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setAirplane(airplane);

        Duration duration = Duration.ofHours(flightDTO.getFlightDurationHours())
                .plusMinutes(flightDTO.getFlightDurationMinutes());
        flight.setFlightDuration(duration);

        Flight savedFlight = flightRepository.save(flight);
        return flightMapper.flightToFlightDTO(savedFlight);
    }

    public List<Integer> getAvailableSeats(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(ErrorType.FLIGHT_NOT_FOUND::create);

        List<Integer> reservedSeats = reservationRepository.findByFlightId(flightId)
                .stream()
                .map(Reservation::getSeatNumber)
                .toList();

        List<Integer> allSeats = IntStream.rangeClosed(1, flight.getAirplane().getTotalSeats())
                .boxed()
                .toList();

        return allSeats.stream()
                .filter(seat -> !reservedSeats.contains(seat))
                .toList();
    }

    public List<Flight> getAvailableFlights(LocalDateTime currentTime) {
        return flightRepository.findAll().stream()
                .filter(flight ->
                        !flight.isCompleted() &&
                                flight.getDepartureDate().isAfter(currentTime)
                )
                .toList();
    }

    public Flight getFlightById(Long flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
    }

}