package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.passenger.PassengerAdminEditDTO;
import com.example.airreservation.model.passenger.PassengerDTO;
import com.example.airreservation.model.passenger.PassengerMapper;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(PassengerService.class);
    private final SpringTemplateEngine templateEngine; 
    private final ReservationRepository reservationRepository;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Transactional
    public PassengerDTO registerNewPassenger(PassengerDTO passengerDTO) {
        if (passengerRepository.existsByEmail(passengerDTO.getEmail())) {
            throw ErrorType.EMAIL_ALREADY_EXISTS.create();
        }
        if (passengerRepository.existsByPhoneNumber(passengerDTO.getPhoneNumber())) {
            throw ErrorType.PHONE_NUMBER_ALREADY_EXISTS.create();
        }

        Passenger passenger = passengerMapper.PassengerDTOToPassenger(passengerDTO);
        passenger.setPassword(passwordEncoder.encode(passengerDTO.getPassword()));
        passenger.setRole("ROLE_USER");
        passenger.setEnabled(false);

        String token = generateConfirmationToken();
        passenger.setConfirmationToken(token);
        passenger.setTokenExpiryDate(LocalDateTime.now().plusHours(24));

        Passenger savedPassenger = passengerRepository.save(passenger);

        try {
            sendConfirmationEmail(savedPassenger, token);
        } catch (MailException e) {
            logger.error("Nie udało się wysłać maila potwierdzającego do {}: {}", savedPassenger.getEmail(), e.getMessage());
        }

        return passengerMapper.PassengerToPassengerDTO(savedPassenger);
    }

    @Transactional
    public boolean confirmUser(String token) {
        Optional<Passenger> passengerOptional = passengerRepository.findByConfirmationToken(token);

        if (passengerOptional.isEmpty()) {
            logger.warn("Próba potwierdzenia z nieprawidłowym tokenem: {}", token);
            return false;
        }

        Passenger passenger = passengerOptional.get();

        if (passenger.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            logger.warn("Próba potwierdzenia z wygasłym tokenem dla emaila: {}", passenger.getEmail());
            passenger.setConfirmationToken(null);
            passenger.setTokenExpiryDate(null);
            passengerRepository.save(passenger);
            return false;
        }

        passenger.setEnabled(true);
        passenger.setConfirmationToken(null);
        passenger.setTokenExpiryDate(null);
        passengerRepository.save(passenger);
        logger.info("Pomyślnie potwierdzono konto dla emaila: {}", passenger.getEmail());
        return true;
    }


    @Async
    public void sendConfirmationEmail(Passenger passenger, String token) { 
        String recipientAddress = passenger.getEmail();
        String subject = "Potwierdzenie rejestracji w AirReservation";
        String confirmationUrl = appBaseUrl + "/confirm?token=" + token;

        
        Context context = new Context();
        context.setVariable("passengerName", passenger.getName()); 
        context.setVariable("confirmationUrl", confirmationUrl); 

        try {
            
            String htmlContent = templateEngine.process("emails/confirmation-email", context); 

            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); 

            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); 

            mailSender.send(mimeMessage); 
            logger.info("Wysłano email HTML potwierdzający rejestrację do: {}", recipientAddress);

        } catch (MessagingException e) {
            logger.error("Błąd podczas tworzenia wiadomości MIME dla potwierdzenia rejestracji ({}): {}", recipientAddress, e.getMessage());
        } catch (MailException e) { 
            logger.error("Nie udało się wysłać maila potwierdzającego rejestrację (MailException) do {}: {}", recipientAddress, e.getMessage());
        } catch (Exception e) { 
            logger.error("Nieoczekiwany błąd podczas wysyłania emaila potwierdzającego rejestrację do {}: {}", recipientAddress, e.getMessage(), e);
        }
    }


    private String generateConfirmationToken() {
        return UUID.randomUUID().toString();
    }

    public boolean existsByEmail(String email) {
        return passengerRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return passengerRepository.existsByPhoneNumber(phoneNumber);
    }

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Passenger> findAllPassengersPaginated(Pageable pageable) {
        return passengerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Passenger> findPassengerById(Long id) {
        return passengerRepository.findById(id);
    }

    @Transactional
    public void updatePassengerByAdmin(PassengerAdminEditDTO dto) {
        Passenger passenger = passengerRepository.findById(dto.getId())
                .orElseThrow(() -> ErrorType.PASSENGER_NOT_FOUND.create(dto.getId()));

        
        if (!passenger.getEmail().equalsIgnoreCase(dto.getEmail()) && passengerRepository.existsByEmail(dto.getEmail())) {
            throw ErrorType.EMAIL_ALREADY_EXISTS.create();
        }
        if (!passenger.getPhoneNumber().equals(dto.getPhoneNumber()) && passengerRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw ErrorType.PHONE_NUMBER_ALREADY_EXISTS.create();
        }

        
        passenger.setName(dto.getName());
        passenger.setSurname(dto.getSurname());
        passenger.setEmail(dto.getEmail());
        passenger.setPhoneNumber(dto.getPhoneNumber());
        passenger.setRole(dto.getRole()); 
        passenger.setEnabled(dto.isEnabled()); 

        
        if (StringUtils.hasText(dto.getNewPassword())) {
            if (dto.getNewPassword().length() < 8) {
                throw ErrorType.TOO_SHORT_PASSWORD.create();
            }
            passenger.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            logger.info("Zmieniono hasło dla pasażera ID {}", dto.getId());
        }

        passengerRepository.save(passenger);
    }

    @Transactional
    public void deletePassenger(Long id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> ErrorType.PASSENGER_NOT_FOUND.create(id));

        
        
        
        if (reservationRepository.existsByPassengerId(id)) { 
            
            
            
            
            logger.warn("Usuwanie pasażera (ID: {}) z istniejącymi rezerwacjami. Rezerwacje NIE zostały usunięte.", id);
        }

        passengerRepository.deleteById(id);
    }
}