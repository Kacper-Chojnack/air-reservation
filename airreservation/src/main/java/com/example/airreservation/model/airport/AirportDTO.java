package com.example.airreservation.model.airport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AirportDTO {
    @NotBlank(message = "Nazwa lotniska jest wymagana")
    private String name;
    @NotNull(message = "Kraj jest wymazany")
    private Long countryId;
}
