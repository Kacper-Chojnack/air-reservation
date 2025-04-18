package com.example.airreservation.model.country;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

}
