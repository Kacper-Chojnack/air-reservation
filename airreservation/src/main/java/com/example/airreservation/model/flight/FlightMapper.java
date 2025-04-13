package com.example.airreservation.model.flight;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    @Mapping(source = "departureAirport.id", target = "departureAirportId")
    @Mapping(source = "arrivalAirport.id", target = "arrivalAirportId")
    @Mapping(source = "airplane.id", target = "airplane")
    FlightDTO flightToFlightDTO(Flight flight);

    @Mapping(target = "departureAirport", ignore = true)
    @Mapping(target = "arrivalAirport", ignore = true)
    @Mapping(target = "airplane", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "id", ignore = true)
    Flight flightDTOToFlight(FlightDTO flightDTO);
}

