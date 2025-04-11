package com.example.airreservation.service;

import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;

@org.springframework.stereotype.Service
public class Service {

    private FlightRepository flightRepository;
    private PassengerRepository passengerRepository;
    private ReservationRepository reservationRepository;

    public Service(FlightRepository flightRepository, PassengerRepository passengerRepository, ReservationRepository reservationRepository){
        this.flightRepository = flightRepository;
        this.passengerRepository = passengerRepository;
        this.reservationRepository = reservationRepository;
    }



}
