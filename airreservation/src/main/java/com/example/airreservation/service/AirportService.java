package com.example.airreservation.service;

import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.repository.AirportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AirportService {

    private AirportRepository airportRepository;

    public AirportService(AirportRepository airportRepository){
        this.airportRepository =  airportRepository;
    }


    public List<Airport> getAllAirports(){
        return airportRepository.findAll();
    }

}
