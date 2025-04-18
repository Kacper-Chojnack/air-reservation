package com.example.airreservation.model.passenger;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PassengerAdminEditDTO {

    @NotNull
    private Long id;

    @NotBlank(message = "Imię jest wymagane")
    private String name;

    @NotBlank(message = "Nazwisko jest wymagane")
    private String surname;

    @NotBlank(message = "Numer jest wymagany")
    @Pattern(regexp = "\\+?[0-9]{9,15}", message = "Nieprawidłowy format numeru telefonu")
    private String phoneNumber;

    @Email(message = "Nieprawidłowy format email")
    @NotBlank(message = "Email jest wymagany")
    private String email;

    private String newPassword;

    @NotBlank(message = "Rola jest wymagana")
    private String role;

    private boolean enabled;
}