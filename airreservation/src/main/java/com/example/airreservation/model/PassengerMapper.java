package com.example.airreservation.model;

import org.mapstruct.Mapper;

@Mapper
public interface PassengerMapper {

    Passenger PassengerDTOToPassenger(PassengerDTO passengerDTO);

    PassengerDTO PassengerToPassengerDTO(Passenger passenger);

}
