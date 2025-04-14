package com.example.airreservation.model.airplane;

import com.example.airreservation.model.flight.Flight;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Entity
@RequiredArgsConstructor
public class Airplane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    private int totalSeats;

    @ToString.Exclude
    @OneToMany(mappedBy = "airplane")
    private List<Flight> flights;

    public Airplane(String name, int totalSeats) {
        this.name = name;
        this.totalSeats = totalSeats;
    }
}
