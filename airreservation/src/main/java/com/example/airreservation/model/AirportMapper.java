package com.example.airreservation.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AirportMapper {

    @Mapping(target = "id", ignore = true)
    Airport airportDTOToAirport(AirportDTO airportDTO);

    AirportDTO AirportToAirportDTO(Airport airport);

}
