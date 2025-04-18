package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.reservation.ReservationDTO;
import com.example.airreservation.service.FlightService;
import com.example.airreservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.repository.PassengerRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final FlightService flightService;
    private final PassengerRepository passengerRepository;

    private void populateCreateModel(Model model, Long flightId, ReservationDTO reservationDTO) {
        var flight = flightService.getFlightById(flightId);
        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("flightId", flightId);
        model.addAttribute("departureAirport", flight.getDepartureAirport().getName());
        model.addAttribute("arrivalAirport", flight.getArrivalAirport().getName());
        model.addAttribute("departureDate", flight.getDepartureDate());
        model.addAttribute("flightDuration", flight.getFlightDuration());
        model.addAttribute("flightNumber", flight.getFlightNumber());
        model.addAttribute("availableSeats", flightService.getAvailableSeats(flightId));
    }

    @GetMapping("/create")
    public String showCreateForm(@RequestParam Long flightId, Model model, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        var flight = flightService.getFlightById(flightId);
        if (flight.isDeparted() || flight.isCompleted()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie można zarezerwować – lot jest już niedostępny.");
            return "redirect:/";
        }

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setFlightId(flightId);
        populateCreateModel(model, flightId, reservationDTO);
        return "reservations/create";
    }

    @PostMapping("/createReservation")
    public String createReservation(
            @Valid @ModelAttribute("reservationDTO") ReservationDTO reservationDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String userEmail = authentication.getName();
        Passenger loggedInPassenger = passengerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono zalogowanego pasażera"));

        reservationDTO.setPassengerId(loggedInPassenger.getId());

        Long flightId = reservationDTO.getFlightId();
        if (flightId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd formularza: Brak identyfikatora lotu.");
            return "redirect:/";
        }
        Flight flight;
        try {
            flight = flightService.getFlightById(flightId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie znaleziono lotu o podanym ID.");
            return "redirect:/";
        }

        if (bindingResult.hasFieldErrors("seatNumber") || bindingResult.hasFieldErrors("flightId")) {
            populateCreateModel(model, flightId, reservationDTO);
            return "reservations/create";
        }

        if (flight.isDeparted() || flight.isCompleted()) {
            model.addAttribute("error", "Nie można zarezerwować – lot jest już niedostępny.");
            populateCreateModel(model, flightId, reservationDTO);
            return "reservations/create";
        }

        try {
            reservationService.saveReservation(reservationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Rezerwacja została pomyślnie utworzona!");
            return "redirect:/";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getReason());
            populateCreateModel(model, flightId, reservationDTO);
            return "reservations/create";
        } catch (Exception e) {
            model.addAttribute("error", "Wystąpił nieoczekiwany błąd podczas próby rezerwacji.");
            populateCreateModel(model, flightId, reservationDTO);
            return "reservations/create";
        }
    }
}