package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.passenger.PassengerDTO;
import com.example.airreservation.service.PassengerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PassengerController.class)
class PassengerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PassengerService passengerService;

    @TestConfiguration
    static class PassengerControllerTestConfiguration {
        @Bean
        PassengerService passengerService() {
            return Mockito.mock(PassengerService.class);
        }
    }

    @Test
    void showCreateForm_shouldReturnCreateView() throws Exception {
        mockMvc.perform(get("/passengers/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("passengers/create"))
                .andExpect(model().attributeExists("passengerDTO"));
    }

    @Test
    void createPassenger_Success_shouldRedirectToLogin() throws Exception {
        when(passengerService.existsByEmail(anyString())).thenReturn(false);
        when(passengerService.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passengerService.registerNewPassenger(any(PassengerDTO.class))).thenReturn(new PassengerDTO());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "Test");
        params.add("surname", "User");
        params.add("email", "test.user@example.com");
        params.add("password", "password123");
        params.add("matchingPassword", "password123");
        params.add("phoneNumber", "555666777");

        mockMvc.perform(post("/passengers")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("registrationSuccessMessage"));

        verify(passengerService).registerNewPassenger(any(PassengerDTO.class));
    }

    @Test
    void createPassenger_ValidationError_shouldReturnCreateView() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email", "not-valid");

        mockMvc.perform(post("/passengers")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("passengers/create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("passengerDTO", "email"));
        verify(passengerService, never()).registerNewPassenger(any(PassengerDTO.class));
    }

    @Test
    void createPassenger_PasswordMismatch_shouldReturnCreateView() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "Test");
        params.add("surname", "User");
        params.add("email", "test@test.com");
        params.add("password", "password123");
        params.add("matchingPassword", "password456");
        params.add("phoneNumber", "123456789");

        when(passengerService.existsByEmail(anyString())).thenReturn(false);
        when(passengerService.existsByPhoneNumber(anyString())).thenReturn(false);

        mockMvc.perform(post("/passengers")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("passengers/create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("passengerDTO", "matchingPassword"));
        verify(passengerService, never()).registerNewPassenger(any(PassengerDTO.class));
    }

    @Test
    void createPassenger_EmailExists_shouldReturnCreateView() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "Test");
        params.add("surname", "User");
        params.add("email", "exists@test.com");
        params.add("password", "password123");
        params.add("matchingPassword", "password123");
        params.add("phoneNumber", "123456789");

        when(passengerService.existsByEmail("exists@test.com")).thenReturn(true);
        when(passengerService.existsByPhoneNumber(anyString())).thenReturn(false);

        mockMvc.perform(post("/passengers")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("passengers/create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("passengerDTO", "email"));
        verify(passengerService, never()).registerNewPassenger(any(PassengerDTO.class));
    }

    @Test
    void createPassenger_BusinessExceptionOnSave_shouldReturnCreateView() throws Exception {
        when(passengerService.existsByEmail(anyString())).thenReturn(false);
        when(passengerService.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passengerService.registerNewPassenger(any(PassengerDTO.class)))
                .thenThrow(ErrorType.EMAIL_ALREADY_EXISTS.create());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "Test");
        params.add("surname", "User");
        params.add("email", "test.user@example.com");
        params.add("password", "password123");
        params.add("matchingPassword", "password123");
        params.add("phoneNumber", "555666777");


        mockMvc.perform(post("/passengers")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("passengers/create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("passengerDTO", "email"));
        verify(passengerService).registerNewPassenger(any(PassengerDTO.class));
    }

    @Test
    void createPassenger_GenericExceptionOnSave_shouldReturnCreateViewWithError() throws Exception {
        when(passengerService.existsByEmail(anyString())).thenReturn(false);
        when(passengerService.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passengerService.registerNewPassenger(any(PassengerDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "Test");
        params.add("surname", "User");
        params.add("email", "test.user@example.com");
        params.add("password", "password123");
        params.add("matchingPassword", "password123");
        params.add("phoneNumber", "555666777");

        mockMvc.perform(post("/passengers")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("passengers/create"))
                .andExpect(model().attributeExists("errorMessage"));
        verify(passengerService).registerNewPassenger(any(PassengerDTO.class));
    }
}