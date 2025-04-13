package com.example.airreservation.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "reservationNumber", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "flightNumber", ignore = true)
    @Mapping(target = "departed", ignore = true)
    Reservation ReservationDTOTOReservation(ReservationDTO reservationDTO);

    @Mapping(source = "passenger.id", target = "passengerId")
    @Mapping(source = "flight.id", target = "flightId")
    ReservationDTO ReservationToReservationDTO(Reservation reservation);
}
