package com.example.airreservation.controller;

import com.example.airreservation.model.passenger.PassengerDTO;
import com.example.airreservation.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("passengerDTO", new PassengerDTO());
        return "passengers/create";
    }

    @PostMapping
    public String createPassenger(
            @Valid @ModelAttribute("passengerDTO") PassengerDTO passengerDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (passengerDTO.getPassword() != null && !passengerDTO.getPassword().equals(passengerDTO.getMatchingPassword())) {
            bindingResult.rejectValue("matchingPassword", "error.passengerDTO", "Hasła muszą być identyczne");
        }

        if (!bindingResult.hasFieldErrors("email") && passengerService.existsByEmail(passengerDTO.getEmail())) {
            bindingResult.rejectValue("email", "error.passengerDTO", "Podany adres email jest już zajęty.");
        }
        if (!bindingResult.hasFieldErrors("phoneNumber") && passengerService.existsByPhoneNumber(passengerDTO.getPhoneNumber())) {
            bindingResult.rejectValue("phoneNumber", "error.passengerDTO", "Podany numer telefonu jest już zajęty.");
        }

        if (bindingResult.hasErrors()) {
            return "passengers/create";
        }

        try {
            passengerService.savePassenger(passengerDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Rejestracja zakończona pomyślnie!");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Wystąpił błąd podczas rejestracji.");
            return "passengers/create";
        }
    }
}