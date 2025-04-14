package com.example.airreservation.model.passenger;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "id", ignore = true)
    Passenger PassengerDTOToPassenger(PassengerDTO passengerDTO);

    PassengerDTO PassengerToPassengerDTO(Passenger passenger);

    @Mapping(target = "newPassword", ignore = true)
    PassengerAdminEditDTO passengerToAdminEditDTO(Passenger passenger);

}
