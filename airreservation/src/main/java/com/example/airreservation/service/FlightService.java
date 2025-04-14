package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.model.flight.FlightMapper;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class FlightService {
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final AirportRepository airportRepository;
    private final AirplaneRepository airplaneRepository;
    private final ReservationRepository reservationRepository;
    private final TemporarySeatLockRepository temporarySeatLockRepository;

    public FlightService(FlightRepository flightRepository, FlightMapper flightMapper,
                         AirportRepository airportRepository, AirplaneRepository airplaneRepository, ReservationRepository reservationRepository, TemporarySeatLockRepository temporarySeatLockRepository) {
        this.flightRepository = flightRepository;
        this.flightMapper = flightMapper;
        this.airportRepository = airportRepository;
        this.airplaneRepository = airplaneRepository;
        this.reservationRepository = reservationRepository;
        this.temporarySeatLockRepository = temporarySeatLockRepository;
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


        List<Integer> lockedSeats = temporarySeatLockRepository.findLockedSeatsByFlightId(flightId, LocalDateTime.now());

        List<Integer> allSeats = IntStream.rangeClosed(1, flight.getAirplane().getTotalSeats())
                .boxed()
                .toList();


        return allSeats.stream()
                .filter(seat -> !reservedSeats.contains(seat) && !lockedSeats.contains(seat))
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

    @Transactional(readOnly = true)
    public Page<Flight> searchFlights(Long departureAirportId, Long arrivalAirportId, LocalDate departureDate, Pageable pageable) {


        LocalDateTime startOfDay = departureDate.atStartOfDay();
        LocalDateTime endOfDay = departureDate.atTime(LocalTime.MAX);


        return flightRepository.findFlightsByCriteria(
                departureAirportId,
                arrivalAirportId,
                startOfDay,
                endOfDay,
                pageable);
    }


    @Transactional(readOnly = true)
    public List<Flight> getUpcomingFlights(int limit) {
        LocalDateTime now = LocalDateTime.now();


        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "departureDate"));

        Page<Flight> upcomingPage = flightRepository.findUpcomingAvailable(now, pageable);

        return upcomingPage.getContent();
    }

    @Transactional(readOnly = true)
    public Page<Flight> findAllFlightsPaginated(Pageable pageable) {
        return flightRepository.findAll(pageable);
    }

    @Transactional
    public FlightDTO updateFlight(FlightDTO flightDTO) {

        Flight existingFlight = flightRepository.findById(flightDTO.getId())
                .orElseThrow(() -> ErrorType.FLIGHT_NOT_FOUND.create(flightDTO.getId()));


        if (flightDTO.getDepartureAirportId().equals(flightDTO.getArrivalAirportId())) {
            throw ErrorType.AIRPORT_SAME_ERROR.create();
        }
        Airport departureAirport = airportRepository.findById(flightDTO.getDepartureAirportId())
                .orElseThrow(ErrorType.AIRPORT_NOT_FOUND::create);
        Airport arrivalAirport = airportRepository.findById(flightDTO.getArrivalAirportId())
                .orElseThrow(ErrorType.AIRPORT_NOT_FOUND::create);
        Airplane airplane = airplaneRepository.findById(flightDTO.getAirplane())
                .orElseThrow(ErrorType.AIRPLANE_NOT_FOUND::create);


        existingFlight.setDepartureAirport(departureAirport);
        existingFlight.setArrivalAirport(arrivalAirport);
        existingFlight.setAirplane(airplane);
        existingFlight.setFlightNumber(flightDTO.getFlightNumber());
        existingFlight.setDepartureDate(flightDTO.getDepartureDate());
        existingFlight.setRoundTrip(flightDTO.isRoundTrip());
        Duration duration = Duration.ofHours(flightDTO.getFlightDurationHours())
                .plusMinutes(flightDTO.getFlightDurationMinutes());
        existingFlight.setFlightDuration(duration);


        Flight updatedFlight = flightRepository.save(existingFlight);
        return flightMapper.flightToFlightDTO(updatedFlight);
    }

    @Transactional
    public void deleteFlight(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> ErrorType.FLIGHT_NOT_FOUND.create(id));


        if (reservationRepository.existsByFlightId(id)) {
            throw ErrorType.FLIGHT_HAS_RESERVATIONS.create(id);
        }


        temporarySeatLockRepository.deleteByFlightId(id);
        flightRepository.deleteById(id);
    }

    public List<Integer> getAvailableSeatsIncludingCurrentLocks(Long flightId, Integer currentSeatNumber) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(ErrorType.FLIGHT_NOT_FOUND::create);

        List<Integer> reservedSeats = reservationRepository.findByFlightId(flightId)
                .stream()
                .map(Reservation::getSeatNumber)

                .filter(seat -> !seat.equals(currentSeatNumber))
                .toList();

        List<Integer> lockedSeats = temporarySeatLockRepository.findLockedSeatsByFlightId(flightId, LocalDateTime.now());

        List<Integer> allSeats = IntStream.rangeClosed(1, flight.getAirplane().getTotalSeats())
                .boxed()
                .toList();

        return allSeats.stream()
                .filter(seat -> !reservedSeats.contains(seat) && !lockedSeats.contains(seat))
                .toList();
    }

}