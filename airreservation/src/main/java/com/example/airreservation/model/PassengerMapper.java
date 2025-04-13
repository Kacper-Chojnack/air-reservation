package com.example.airreservation.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "id", ignore = true)
    Passenger PassengerDTOToPassenger(PassengerDTO passengerDTO);

    PassengerDTO PassengerToPassengerDTO(Passenger passenger);

}
