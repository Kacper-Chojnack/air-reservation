package com.example.airreservation.model;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-13T14:51:50+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class ReservationMapperImpl implements ReservationMapper {

    @Override
    public Reservation ReservationDTOTOReservation(ReservationDTO reservationDTO) {
        if ( reservationDTO == null ) {
            return null;
        }

        Reservation reservation = new Reservation();

        reservation.setSeatNumber( reservationDTO.getSeatNumber() );

        return reservation;
    }

    @Override
    public ReservationDTO ReservationToReservationDTO(Reservation reservation) {
        if ( reservation == null ) {
            return null;
        }

        ReservationDTO reservationDTO = new ReservationDTO();

        Long id = reservationPassengerId( reservation );
        if ( id != null ) {
            reservationDTO.setPassengerId( id );
        }
        Long id1 = reservationFlightId( reservation );
        if ( id1 != null ) {
            reservationDTO.setFlightId( id1 );
        }
        reservationDTO.setReservationNumber( reservation.getReservationNumber() );
        reservationDTO.setFlightNumber( reservation.getFlightNumber() );
        reservationDTO.setSeatNumber( reservation.getSeatNumber() );
        reservationDTO.setFullName( reservation.getFullName() );
        reservationDTO.setEmail( reservation.getEmail() );
        reservationDTO.setPhoneNumber( reservation.getPhoneNumber() );
        reservationDTO.setDeparted( reservation.isDeparted() );

        return reservationDTO;
    }

    private Long reservationPassengerId(Reservation reservation) {
        Passenger passenger = reservation.getPassenger();
        if ( passenger == null ) {
            return null;
        }
        return passenger.getId();
    }

    private Long reservationFlightId(Reservation reservation) {
        Flight flight = reservation.getFlight();
        if ( flight == null ) {
            return null;
        }
        return flight.getId();
    }
}
