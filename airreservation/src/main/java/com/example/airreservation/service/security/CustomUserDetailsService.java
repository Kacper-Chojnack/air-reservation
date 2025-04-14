package com.example.airreservation.service.security;

import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PassengerRepository passengerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Passenger passenger = passengerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika o emailu: " + email));

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(passenger.getRole()));

        return new User(
                passenger.getEmail(),
                passenger.getPassword(),
                authorities
        );
    }
}