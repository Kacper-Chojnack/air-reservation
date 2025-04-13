package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.AirplaneNotFoundException;
import com.example.airreservation.exceptionHandler.AirportNotFoundException;
import com.example.airreservation.model.*;
import com.example.airreservation.repository.AirplaneRepository;
import com.example.airreservation.repository.AirportRepository;
import com.example.airreservation.repository.FlightRepository;
import org.springframework.stereotype.Service;

@Service
public class FlightService {
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final AirportRepository airportRepository;
    private final AirplaneRepository airplaneRepository;

    public FlightService(FlightRepository flightRepository, FlightMapper flightMapper, AirportRepository airportRepository, AirplaneRepository airplaneRepository) {
        this.flightRepository = flightRepository;
        this.flightMapper = flightMapper;
        this.airportRepository = airportRepository;
        this.airplaneRepository = airplaneRepository;
    }

    public FlightDTO saveFlight(FlightDTO flightDTO) {
        Flight flight = flightMapper.flightDTOToFlight(flightDTO);

        Airport departureAirport = airportRepository.findById(flightDTO.getDepartureAirportId())
                .orElseThrow(() -> new AirportNotFoundException());

        Airport arrivalAirport = airportRepository.findById(flightDTO.getArrivalAirportId())
                .orElseThrow(() -> new AirportNotFoundException());

        Airplane airplane = airplaneRepository.findById(flightDTO.getAirplane())
                        .orElseThrow(() -> new AirplaneNotFoundException());

        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setAirplane(airplane);

        Flight savedFlight = flightRepository.save(flight);
        return flightMapper.flightToFlightDTO(savedFlight);
    }
}