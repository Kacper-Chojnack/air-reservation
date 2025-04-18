package com.example.airreservation.model.reservation;

import com.example.airreservation.model.flight.Flight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.format.DateTimeFormatter;

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

    @Mapping(target = "passengerFullName", expression = "java(reservation.getPassenger() != null ? reservation.getPassenger().getName() + \" \" + reservation.getPassenger().getSurname() : \"Brak danych pasażera\")")
    @Mapping(source = "reservation", target = "flightInfo", qualifiedByName = "flightToInfoString")
    ReservationAdminEditDTO reservationToAdminEditDTO(Reservation reservation);

    @Named("flightToInfoString")
    default String flightToInfoString(Reservation reservation) {
        if (reservation.getFlight() == null) {
            return "Brak informacji o locie";
        }
        Flight flight = reservation.getFlight();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String depAirport = flight.getDepartureAirport() != null ? flight.getDepartureAirport().getName() : "?";
        String arrAirport = flight.getArrivalAirport() != null ? flight.getArrivalAirport().getName() : "?";
        String depDate = flight.getDepartureDate() != null ? flight.getDepartureDate().format(formatter) : "?";
        return String.format("%s - %s -> %s (%s)",
                flight.getFlightNumber() != null ? flight.getFlightNumber() : "?",
                depAirport, arrAirport, depDate);
    }
}
