package com.example.airreservation.controller;

import com.example.airreservation.service.PassengerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PassengerService passengerService;

    @TestConfiguration
    static class AuthControllerTestConfiguration {
        @Bean
        public PassengerService passengerService() {
            return Mockito.mock(PassengerService.class);
        }
    }

    @Test
    void showLoginPage_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void confirmRegistration_Success_shouldRedirectToLoginWithSuccessFlash() throws Exception {
        String token = "valid-token";
        when(passengerService.confirmUser(token)).thenReturn(true);

        mockMvc.perform(get("/confirm").param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("confirmationSuccess"));

        verify(passengerService).confirmUser(token);
    }

    @Test
    void confirmRegistration_Failure_shouldRedirectToLoginWithErrorFlash() throws Exception {
        String token = "invalid-or-expired-token";
        when(passengerService.confirmUser(token)).thenReturn(false);

        mockMvc.perform(get("/confirm").param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("confirmationError"));

        verify(passengerService).confirmUser(token);
    }
}