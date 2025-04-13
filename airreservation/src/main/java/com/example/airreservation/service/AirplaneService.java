package com.example.airreservation.service;

import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.repository.AirplaneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AirplaneService {

    private AirplaneRepository airplaneRepository;

    public AirplaneService(AirplaneRepository airplaneRepository){
        this.airplaneRepository =  airplaneRepository;
    }


    public List<Airplane> getAllAirplanes(){
        return airplaneRepository.findAll();
    }

}
