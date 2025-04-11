package com.example.airreservation.model;

import org.mapstruct.Mapper;

@Mapper
public interface FlightMapper {

    FlightDTO flightToFlightDTO(Flight flight);

    Flight flightDTOToFlight(FlightDTO flightDTO);
}
