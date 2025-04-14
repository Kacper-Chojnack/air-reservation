package com.example.airreservation.model.reservation;

import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.passenger.Passenger;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-14T16:00:25+0200",
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

        reservationDTO.setPassengerId( reservationPassengerId( reservation ) );
        reservationDTO.setFlightId( reservationFlightId( reservation ) );
        reservationDTO.setReservationNumber( reservation.getReservationNumber() );
        reservationDTO.setFlightNumber( reservation.getFlightNumber() );
        reservationDTO.setFullName( reservation.getFullName() );
        reservationDTO.setEmail( reservation.getEmail() );
        reservationDTO.setPhoneNumber( reservation.getPhoneNumber() );
        reservationDTO.setDeparted( reservation.isDeparted() );
        reservationDTO.setSeatNumber( reservation.getSeatNumber() );

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
