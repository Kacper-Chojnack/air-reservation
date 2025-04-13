package com.example.airreservation.controller;

import com.example.airreservation.model.FlightDTO;
import com.example.airreservation.model.PassengerDTO;
import com.example.airreservation.model.ReservationDTO;
import com.example.airreservation.service.FlightService;
import com.example.airreservation.service.PassengerService;
import com.example.airreservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private PassengerService passengerService;
    private FlightService flightService;
    private ReservationService reservationService;

    public Controller(PassengerService passengerService,
                      FlightService flightService,
                      ReservationService reservationService) {
        this.passengerService = passengerService;
        this.flightService = flightService;
        this.reservationService = reservationService;
    }

    @PostMapping("/createPassenger")
    public ResponseEntity<PassengerDTO> createNewPassenger(@Valid @RequestBody PassengerDTO passengerDTO) {
        passengerService.savePassenger(passengerDTO);
        return new ResponseEntity(passengerDTO, HttpStatus.CREATED);
    }

    @PostMapping("/createFlight")
    public ResponseEntity<FlightDTO> createNewFlight(@Valid @RequestBody FlightDTO flightDTO){
        flightService.saveFlight(flightDTO);
        return new ResponseEntity<>(flightDTO, HttpStatus.CREATED);
    }

    @PostMapping("/createReservation")
    public ResponseEntity<ReservationDTO> createNewReservation(@Valid @RequestBody ReservationDTO reservationDTO){
        reservationService.saveReservation(reservationDTO);
        return new ResponseEntity<>(reservationDTO, HttpStatus.CREATED);
    }
}
