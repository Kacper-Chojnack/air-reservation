package com.example.airreservation.model.passenger;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-14T22:56:54+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class PassengerMapperImpl implements PassengerMapper {

    @Override
    public Passenger PassengerDTOToPassenger(PassengerDTO passengerDTO) {
        if ( passengerDTO == null ) {
            return null;
        }

        Passenger passenger = new Passenger();

        passenger.setName( passengerDTO.getName() );
        passenger.setSurname( passengerDTO.getSurname() );
        passenger.setPhoneNumber( passengerDTO.getPhoneNumber() );
        passenger.setEmail( passengerDTO.getEmail() );
        passenger.setPassword( passengerDTO.getPassword() );

        return passenger;
    }

    @Override
    public PassengerDTO PassengerToPassengerDTO(Passenger passenger) {
        if ( passenger == null ) {
            return null;
        }

        PassengerDTO passengerDTO = new PassengerDTO();

        passengerDTO.setName( passenger.getName() );
        passengerDTO.setSurname( passenger.getSurname() );
        passengerDTO.setPhoneNumber( passenger.getPhoneNumber() );
        passengerDTO.setEmail( passenger.getEmail() );
        passengerDTO.setPassword( passenger.getPassword() );

        return passengerDTO;
    }

    @Override
    public PassengerAdminEditDTO passengerToAdminEditDTO(Passenger passenger) {
        if ( passenger == null ) {
            return null;
        }

        PassengerAdminEditDTO passengerAdminEditDTO = new PassengerAdminEditDTO();

        passengerAdminEditDTO.setId( passenger.getId() );
        passengerAdminEditDTO.setName( passenger.getName() );
        passengerAdminEditDTO.setSurname( passenger.getSurname() );
        passengerAdminEditDTO.setPhoneNumber( passenger.getPhoneNumber() );
        passengerAdminEditDTO.setEmail( passenger.getEmail() );
        passengerAdminEditDTO.setRole( passenger.getRole() );
        passengerAdminEditDTO.setEnabled( passenger.isEnabled() );

        return passengerAdminEditDTO;
    }
}
