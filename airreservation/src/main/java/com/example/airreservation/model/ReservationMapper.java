package com.example.airreservation.model;

import org.mapstruct.Mapper;

@Mapper
public interface ReservationMapper {

    Reservation ReservationDTOTOReservation(ReservationDTO reservationDTO);

    ReservationDTO ReservationToReservationDTO(Reservation reservation);
}
