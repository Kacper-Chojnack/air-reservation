package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.model.passenger.PassengerDTO;
import com.example.airreservation.model.reservation.ReservationDTO;
import com.example.airreservation.service.*;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Controller
public class Controller {

    private PassengerService passengerService;
    private FlightService flightService;
    private ReservationService reservationService;
    private AirportService airportService;
    private AirplaneService airplaneService;

    public Controller(PassengerService passengerService,
                      FlightService flightService,
                      ReservationService reservationService,
                      AirplaneService airplaneService,
                      AirportService airportService) {
        this.passengerService = passengerService;
        this.flightService = flightService;
        this.reservationService = reservationService;
        this.airplaneService = airplaneService;
        this.airportService = airportService;
    }

    @GetMapping("/createPassengerForm")
    public String showCreatePassengerForm(Model model) {
        model.addAttribute("passengerDTO", new PassengerDTO());
        return "createPassenger";
    }

    @PostMapping("/createPassenger")
    public String createNewPassenger(@Valid @ModelAttribute PassengerDTO passengerDTO) {
        passengerService.savePassenger(passengerDTO);
        return "redirect:/";
    }

//    @GetMapping("/createFlightForm")
//    public String showCreateFlightForm(Model model) {
//        model.addAttribute("flightDTO", new FlightDTO());
//        model.addAttribute("airports", airportService.getAllAirports());
//        model.addAttribute("airplanes", airplaneService.getAllAirplanes());
//        return "createFlight";
//    }
//
//    @PostMapping("/createFlight")
//    public String createNewFlight(@Valid @ModelAttribute FlightDTO flightDTO) {
//        flightService.saveFlight(flightDTO);
//        return "redirect:/";
//    }

    @GetMapping("/createFlightForm")
    public String showCreateForm(Model model) {
        model.addAttribute("flightDTO", new FlightDTO());
        loadDropdownData(model);
        return "createFlight";
    }

    @PostMapping("/createFlight")
    public String createFlight(
            @Valid @ModelAttribute FlightDTO flightDTO,
            BindingResult bindingResult,
            Model model
    ) {
        if (flightDTO.getDepartureAirportId() != null
                && flightDTO.getArrivalAirportId() != null
                && flightDTO.getDepartureAirportId().equals(flightDTO.getArrivalAirportId())) {
            bindingResult.rejectValue("departureAirportId", "airportError", ErrorType.AIRPORT_SAME_ERROR.create().getReason());
        }

        if (bindingResult.hasErrors()) {
            loadDropdownData(model);
            return "createFlight";
        }

        flightService.saveFlight(flightDTO);
        return "redirect:/";
    }

    @PostMapping("/createReservation")
    public String createNewReservation(
            @Valid @ModelAttribute ReservationDTO reservationDTO,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("passengers", passengerService.getAllPassengers());
            model.addAttribute("flights", flightService.getAvailableFlights(LocalDateTime.now()));
            return "createReservation";
        }

        try {
            reservationService.saveReservation(reservationDTO);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getReason());
            model.addAttribute("passengers", passengerService.getAllPassengers());
            model.addAttribute("flights", flightService.getAvailableFlights(LocalDateTime.now()));
            return "createReservation";
        }

        return "redirect:/";
    }


    private void loadDropdownData(Model model) {
        model.addAttribute("airports", airportService.getAllAirports());
        model.addAttribute("airplanes", airplaneService.getAllAirplanes());
    }

    @GetMapping("/getAvailableSeats")
    @ResponseBody
    public List<Integer> getAvailableSeats(@RequestParam Long flightId) {
        return flightService.getAvailableSeats(flightId);
    }

    @GetMapping("/createReservationForm")
    public String showCreateReservationForm(Model model) {
        model.addAttribute("reservationDTO", new ReservationDTO());
        model.addAttribute("passengers", passengerService.getAllPassengers());
        model.addAttribute("flights", flightService.getAvailableFlights(LocalDateTime.now()));
        return "createReservation";
    }



    @GetMapping("/")
    public String index(Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<Flight> visibleFlights = flightService.getAvailableFlights(now);

        visibleFlights.forEach(flight -> {
            Duration duration = flight.getFlightDuration();
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            flight.setFormattedDuration(hours + "h " + minutes + "min");
        });

        model.addAttribute("flights", visibleFlights);

        return "index";
    }
}
