package com.example.airreservation.model.airport;

import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-13T22:20:41+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.6 (Amazon.com Inc.)"
)
public class AirportMapperImpl implements AirportMapper {

    @Override
    public Airport airportDTOToAirport(AirportDTO airportDTO) {
        if ( airportDTO == null ) {
            return null;
        }

        Airport airport = new Airport();

        airport.setName( airportDTO.getName() );

        return airport;
    }

    @Override
    public AirportDTO AirportToAirportDTO(Airport airport) {
        if ( airport == null ) {
            return null;
        }

        AirportDTO airportDTO = new AirportDTO();

        airportDTO.setName( airport.getName() );

        return airportDTO;
    }
}
