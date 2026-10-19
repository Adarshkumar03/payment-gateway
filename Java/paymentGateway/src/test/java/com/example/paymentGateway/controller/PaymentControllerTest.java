package com.example.paymentGateway.controller;

import com.example.paymentGateway.dto.PaymentResponseDTO;
import com.example.paymentGateway.model.PaymentStatus;
import com.example.paymentGateway.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentService paymentService;

    private static final String BODY = """
            {"fromAccountId": 1, "toAccountId": 2, "amount": 250.00}
            """;

    @Test
    @WithMockUser(username = "alice")
    void shouldReturn400WhenIdempotencyKeyHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required header: Idempotency-Key"));
    }

    @Test
    @WithMockUser(username = "alice")
    void shouldCreatePaymentWhenRequestIsValid() throws Exception {
        var response = new PaymentResponseDTO(1L, 1L, 2L, new BigDecimal("250.00"),
                PaymentStatus.COMPLETED, "order-1", Instant.now());
        when(paymentService.transfer(any(), eq("order-1"), eq("alice"))).thenReturn(response);

        mockMvc.perform(post("/payments")
                        .with(csrf())
                        .header("Idempotency-Key", "order-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldReturn401WhenNotLoggedIn() throws Exception {
        mockMvc.perform(post("/payments")
                        .with(csrf())
                        .header("Idempotency-Key", "order-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }
}