package com.example.airreservation.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

}
