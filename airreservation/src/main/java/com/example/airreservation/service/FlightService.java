package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.AirplaneNotFoundException;
import com.example.airreservation.exceptionHandler.AirportNotFoundException;
import com.example.airreservation.exceptionHandler.FlightNotFoundException;
import com.example.airreservation.exceptionHandler.InvalidFlightException;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.model.flight.FlightMapper;
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
                .orElseThrow(() -> new AirportNotFoundException());

        Airport arrivalAirport = airportRepository.findById(flightDTO.getArrivalAirportId())
                .orElseThrow(() -> new AirportNotFoundException());

        Airplane airplane = airplaneRepository.findById(flightDTO.getAirplane())
                .orElseThrow(() -> new AirplaneNotFoundException());

        if (flightDTO.getDepartureAirportId().equals(flightDTO.getArrivalAirportId())) {
            throw new InvalidFlightException();
        }

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
                .orElseThrow(() -> new FlightNotFoundException());

        // Pobierz zajęte miejsca z aktywnych rezerwacji
        List<Integer> reservedSeats = reservationRepository.findActiveSeatsByFlightId(flightId);

        // Wygeneruj listę wszystkich miejsc w samolocie
        List<Integer> allSeats = IntStream.rangeClosed(1, flight.getAirplane().getTotalSeats())
                .boxed()
                .toList();

        // Filtruj wolne miejsca
        return allSeats.stream()
                .filter(seat -> !reservedSeats.contains(seat))
                .toList();
    }

    public List<Flight> getAvailableFlights(LocalDateTime localDateTime){
        return flightRepository.findVisibleFlights(localDateTime);
    }
}