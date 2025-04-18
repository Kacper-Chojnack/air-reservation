package com.example.airreservation.model.country;

import com.example.airreservation.repository.CountryRepository;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Entity
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    public Country(String name){
        this.name = name;
    }

    public Country(){}

}
