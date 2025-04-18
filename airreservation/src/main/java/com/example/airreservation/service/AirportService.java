package com.example.airreservation.service;

import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepository;


    public List<Airport> getAllAirports(){
        return airportRepository.findAll();
    }

}
