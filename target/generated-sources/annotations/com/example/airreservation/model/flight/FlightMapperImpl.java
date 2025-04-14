package com.example.airreservation.model.flight;

import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-14T16:00:25+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class FlightMapperImpl implements FlightMapper {

    @Override
    public FlightDTO flightToFlightDTO(Flight flight) {
        if ( flight == null ) {
            return null;
        }

        FlightDTO flightDTO = new FlightDTO();

        flightDTO.setDepartureAirportId( flightDepartureAirportId( flight ) );
        flightDTO.setArrivalAirportId( flightArrivalAirportId( flight ) );
        flightDTO.setAirplane( flightAirplaneId( flight ) );
        flightDTO.setFlightNumber( flight.getFlightNumber() );
        flightDTO.setRoundTrip( flight.isRoundTrip() );
        flightDTO.setDepartureDate( flight.getDepartureDate() );

        return flightDTO;
    }

    @Override
    public Flight flightDTOToFlight(FlightDTO flightDTO) {
        if ( flightDTO == null ) {
            return null;
        }

        Flight flight = new Flight();

        flight.setFlightNumber( flightDTO.getFlightNumber() );
        flight.setRoundTrip( flightDTO.isRoundTrip() );
        flight.setDepartureDate( flightDTO.getDepartureDate() );

        return flight;
    }

    private Long flightDepartureAirportId(Flight flight) {
        Airport departureAirport = flight.getDepartureAirport();
        if ( departureAirport == null ) {
            return null;
        }
        return departureAirport.getId();
    }

    private Long flightArrivalAirportId(Flight flight) {
        Airport arrivalAirport = flight.getArrivalAirport();
        if ( arrivalAirport == null ) {
            return null;
        }
        return arrivalAirport.getId();
    }

    private Long flightAirplaneId(Flight flight) {
        Airplane airplane = flight.getAirplane();
        if ( airplane == null ) {
            return null;
        }
        return airplane.getId();
    }
}
