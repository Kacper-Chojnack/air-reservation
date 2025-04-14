package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.service.AirplaneService;
import com.example.airreservation.service.AirportService;
import com.example.airreservation.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final AirportService airportService;
    private final AirplaneService airplaneService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("flightDTO", new FlightDTO());
        loadDropdownData(model);
        return "flights/create";
    }

    @PostMapping
    public String createFlight(
            @Valid @ModelAttribute("flightDTO") FlightDTO flightDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            loadDropdownData(model);
            return "flights/create";
        }

        try {
            flightService.saveFlight(flightDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Lot utworzony pomyślnie!");
            return "redirect:/";
        } catch (BusinessException e) {
            if (e.getErrorType() == ErrorType.AIRPORT_SAME_ERROR) {
                bindingResult.rejectValue("arrivalAirportId", "error.flightDTO", e.getReason());
                loadDropdownData(model);
                return "flights/create";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd biznesowy: " + e.getReason());
                return "redirect:/flights/create";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił nieoczekiwany błąd.");
            return "redirect:/flights/create";
        }
    }


    private void loadDropdownData(Model model) {
        model.addAttribute("airports", airportService.getAllAirports());
        model.addAttribute("airplanes", airplaneService.getAllAirplanes());
    }
}