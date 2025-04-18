package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.model.flight.FlightMapper;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.passenger.PassengerAdminEditDTO;
import com.example.airreservation.model.passenger.PassengerMapper;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.reservation.ReservationAdminEditDTO;
import com.example.airreservation.model.reservation.ReservationMapper;
import com.example.airreservation.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final FlightService flightService;
    private final PassengerService passengerService;
    private final ReservationService reservationService;
    private final AirportService airportService;
    private final AirplaneService airplaneService;
    private final FlightMapper flightMapper;
    private final PassengerMapper passengerMapper;
    private final ReservationMapper reservationMapper;


    @GetMapping
    public String adminDashboard() {
        return "admin/index";
    }


    @GetMapping("/flights")
    public String listFlights(@PageableDefault(size = 15, sort = "departureDate") Pageable pageable, Model model) {

        Page<Flight> flightPage = flightService.findAllFlightsPaginated(pageable);
        model.addAttribute("flightPage", flightPage);
        return "admin/flights/list";
    }

    @GetMapping("/flights/edit/{id}")
    public String showEditFlightForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Flight flight = flightService.getFlightById(id);
            FlightDTO flightDTO = flightMapper.flightToFlightDTO(flight);

            flightDTO.setId(flight.getId());
            if (flight.getFlightDuration() != null) {
                flightDTO.setFlightDurationHours((int) flight.getFlightDuration().toHours());
                flightDTO.setFlightDurationMinutes(flight.getFlightDuration().toMinutesPart());
            }
            model.addAttribute("flightDTO", flightDTO);
            model.addAttribute("airports", airportService.getAllAirports());
            model.addAttribute("airplanes", airplaneService.getAllAirplanes());
            return "admin/flights/edit";
        } catch (Exception e) {
            logger.error("Błąd pobierania lotu do edycji (ID: {})", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Nie znaleziono lotu o ID: " + id);
            return "redirect:/admin/flights";
        }
    }

    @PostMapping("/flights/update")
    public String updateFlight(@Valid @ModelAttribute("flightDTO") FlightDTO flightDTO,
                               BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("airports", airportService.getAllAirports());
            model.addAttribute("airplanes", airplaneService.getAllAirplanes());
            return "admin/flights/edit";
        }
        try {
            flightService.updateFlight(flightDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Lot zaktualizowany pomyślnie.");
        } catch (Exception e) {
            logger.error("Błąd aktualizacji lotu (ID: {})", flightDTO.getId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd aktualizacji lotu: " + e.getMessage());
        }
        return "redirect:/admin/flights";
    }

    @PostMapping("/flights/delete/{id}")
    public String deleteFlight(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            flightService.deleteFlight(id);
            redirectAttributes.addFlashAttribute("successMessage", "Lot usunięty pomyślnie.");
        } catch (BusinessException e) {
            logger.warn("Nie można usunąć lotu (ID: {}): {}", id, e.getReason());
            redirectAttributes.addFlashAttribute("errorMessage", "Nie można usunąć lotu: " + e.getReason());
        } catch (Exception e) {
            logger.error("Błąd usuwania lotu (ID: {})", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas usuwania lotu.");
        }
        return "redirect:/admin/flights";
    }


    @GetMapping("/passengers")
    public String listPassengers(@PageableDefault(size = 15, sort = "surname") Pageable pageable, Model model) {
        Page<Passenger> passengerPage = passengerService.findAllPassengersPaginated(pageable);
        model.addAttribute("passengerPage", passengerPage);
        return "admin/passengers/list";
    }

    @GetMapping("/passengers/edit/{id}")
    public String showEditPassengerForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Passenger> passengerOpt = passengerService.findPassengerById(id);
        if (passengerOpt.isPresent()) {

            PassengerAdminEditDTO dto = passengerMapper.passengerToAdminEditDTO(passengerOpt.get());
            model.addAttribute("passengerAdminEditDTO", dto);
            return "admin/passengers/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie znaleziono pasażera o ID: " + id);
            return "redirect:/admin/passengers";
        }
    }

    @PostMapping("/passengers/update")
    public String updatePassenger(@Valid @ModelAttribute("passengerAdminEditDTO") PassengerAdminEditDTO dto,
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {


        if (bindingResult.hasErrors()) {


            redirectAttributes.addFlashAttribute("errorMessage", "Błąd walidacji danych pasażera.");
            logger.warn("Błąd walidacji DTO przy edycji pasażera ID {}: {}", dto.getId(), bindingResult.getAllErrors());
            return "admin/passengers/edit";
        }

        try {
            passengerService.updatePassengerByAdmin(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Dane pasażera zaktualizowane pomyślnie.");
            return "redirect:/admin/passengers";
        } catch (BusinessException e) {
            logger.warn("Błąd biznesowy podczas aktualizacji pasażera ID {}: {}", dto.getId(), e.getReason());
            if (e.getErrorType() == ErrorType.EMAIL_ALREADY_EXISTS) {
                bindingResult.rejectValue("email", "error.passengerAdminEditDTO", e.getReason());
            } else if (e.getErrorType() == ErrorType.PHONE_NUMBER_ALREADY_EXISTS) {
                bindingResult.rejectValue("phoneNumber", "error.passengerAdminEditDTO", e.getReason());
            } else if (e.getErrorType() == ErrorType.TOO_SHORT_PASSWORD) {
                bindingResult.rejectValue("newPassword", "error.passengerAdminEditDTO", e.getReason());
            } else {
                bindingResult.reject("error.passengerAdminEditDTO", e.getReason());
            }
            return "admin/passengers/edit";
        } catch (Exception e) {
            logger.error("Błąd aktualizacji pasażera (ID: {})", dto.getId(), e);
            model.addAttribute("errorMessage", "Wystąpił nieoczekiwany błąd podczas aktualizacji.");
            return "admin/passengers/edit";
        }
    }


    @PostMapping("/passengers/delete/{id}")
    public String deletePassenger(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            passengerService.deletePassenger(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pasażer usunięty pomyślnie.");
        } catch (BusinessException e) {
            logger.warn("Nie można usunąć pasażera (ID: {}): {}", id, e.getReason());
            redirectAttributes.addFlashAttribute("errorMessage", "Nie można usunąć pasażera: " + e.getReason());
        } catch (Exception e) {
            logger.error("Błąd usuwania pasażera (ID: {})", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas usuwania pasażera.");
        }
        return "redirect:/admin/passengers";
    }


    @GetMapping("/reservations")
    public String listReservations(@PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        Page<Reservation> reservationPage = reservationService.findAllReservationsPaginated(pageable);
        model.addAttribute("reservationPage", reservationPage);
        return "admin/reservations/list";
    }


    @GetMapping("/reservations/edit/{id}")
    public String showEditReservationForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Reservation> reservationOpt = reservationService.findReservationById(id);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            ReservationAdminEditDTO dto = reservationMapper.reservationToAdminEditDTO(reservation);
            model.addAttribute("reservationAdminEditDTO", dto);


            if (reservation.getFlight() != null) {

                List<Integer> availableSeatsForEdit = flightService.getAvailableSeatsIncludingCurrentLocks(
                        reservation.getFlight().getId(),
                        reservation.getSeatNumber()
                );
                model.addAttribute("availableSeats", availableSeatsForEdit);
            } else {
                model.addAttribute("availableSeats", Collections.emptyList());
            }

            return "admin/reservations/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie znaleziono rezerwacji o ID: " + id);
            return "redirect:/admin/reservations";
        }
    }


    @PostMapping("/reservations/update")
    public String updateReservation(@Valid @ModelAttribute("reservationAdminEditDTO") ReservationAdminEditDTO dto,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd walidacji danych rezerwacji.");
            logger.warn("Błąd walidacji DTO przy edycji rezerwacji ID {}: {}", dto.getId(), bindingResult.getAllErrors());

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reservationAdminEditDTO", bindingResult);
            redirectAttributes.addFlashAttribute("reservationAdminEditDTO", dto);
            return "redirect:/admin/reservations/edit/" + dto.getId();
        }

        try {
            reservationService.updateReservationByAdmin(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Rezerwacja zaktualizowana pomyślnie.");
        } catch (BusinessException e) {
            logger.warn("Błąd biznesowy podczas aktualizacji rezerwacji ID {}: {}", dto.getId(), e.getReason());
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd aktualizacji: " + e.getReason());

            redirectAttributes.addFlashAttribute("reservationAdminEditDTO", dto);
            return "redirect:/admin/reservations/edit/" + dto.getId();
        } catch (Exception e) {
            logger.error("Błąd aktualizacji rezerwacji (ID: {})", dto.getId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił nieoczekiwany błąd podczas aktualizacji.");
            return "redirect:/admin/reservations/edit/" + dto.getId();
        }
        return "redirect:/admin/reservations";
    }


    @PostMapping("/reservations/delete/{id}")
    public String deleteReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.deleteReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Rezerwacja usunięta pomyślnie.");
        } catch (BusinessException e) {
            logger.warn("Nie można usunąć rezerwacji (ID: {}): {}", id, e.getReason());
            redirectAttributes.addFlashAttribute("errorMessage", "Nie można usunąć rezerwacji: " + e.getReason());
        } catch (Exception e) {
            logger.error("Błąd usuwania rezerwacji (ID: {})", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas usuwania rezerwacji.");
        }
        return "redirect:/admin/reservations";
    }

}