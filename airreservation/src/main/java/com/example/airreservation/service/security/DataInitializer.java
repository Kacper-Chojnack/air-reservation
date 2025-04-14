package com.example.airreservation.service.security;

import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!passengerRepository.existsByEmail("k@wp.pl")) {
            Passenger admin = new Passenger();
            admin.setName("Admin");
            admin.setSurname("Adminowski");
            admin.setEmail("k@wp.pl");
            admin.setPassword(passwordEncoder.encode("adminpass"));
            admin.setPhoneNumber("000000000");
            admin.setRole("ROLE_ADMIN");
            passengerRepository.save(admin);
        }
    }
}