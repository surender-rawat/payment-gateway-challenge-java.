package com.checkout.payment.gateway.controller;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.Payment;
import com.checkout.payment.gateway.service.IdempotencyService;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @MockBean
  private PaymentGatewayService paymentGatewayService;

  @MockBean
  private IdempotencyService idempotencyService;

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    Payment payment = new Payment.Builder().id(UUID.randomUUID())
        .amount(10).currency("USD").status(PaymentStatus.AUTHORIZED)
        .expiryMonth(12).expiryYear(2024).cardNumber("123456789011112").build();
    when(paymentGatewayService.getPaymentById(any())).thenReturn(payment);
    mvc.perform(MockMvcRequestBuilders.get("/api/payments/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFourDigit").value(
            payment.getCardNumber().substring(payment.getCardNumber().length() - 4)))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    when(paymentGatewayService.getPaymentById(any())).thenThrow(PaymentNotFoundException.class);
    mvc.perform(MockMvcRequestBuilders.get("/api/payments/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors.[*].message").value("Page not found"));
  }

  @Test
  void createPayment_missingCardNumber_returnsBadRequest() throws Exception {
    String json = """
        {
          "expiry_month": 12,
          "expiry_year": 2099,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("cardNumber")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(org.hamcrest.Matchers.hasItem("Card number is required")));
  }

  @Test
  void createPayment_cardNumberLengthError_returnsBadRequest() throws Exception {
    String json = """
        {
        "card_number": "222240534324",
          "expiry_month": 12,
          "expiry_year": 2099,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("cardNumber")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(org.hamcrest.Matchers.hasItem("Card number must be between 14 and 19 digits")));
  }

  @Test
  void createPayment_cardNumberWithAlphaNumeric_returnsBadRequest() throws Exception {
    String json = """
        {
        "card_number": "2222405343241234AS",
          "expiry_month": 12,
          "expiry_year": 2099,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("cardNumber")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(
                org.hamcrest.Matchers.hasItem("Card number must contain only numeric characters")));
  }

  @Test
  void createPayment_expiryMonthMissing_returnsBadRequest() throws Exception {
    String json = """
        {
        "card_number": "2222405343241234",
          "expiry_year": 2099,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("expiryMonth")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(org.hamcrest.Matchers.hasItem("Expiry month is required")));
  }

  @Test
  void createPayment_expiryMonthInvalid_returnsBadRequest() throws Exception {
    String json = """
        {
        "card_number": "2222405343241234",
         "expiry_month": 13,
          "expiry_year": 2099,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("expiryMonth")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(org.hamcrest.Matchers.hasItem("Expiry month must be between 1 and 12")));
  }

  @Test
  void createPayment_expiryYearInvalid_returnsBadRequest() throws Exception {
    String json = """
        {
        "card_number": "2222405343241234",
         "expiry_month": 10,
          "expiry_year": 2019,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("expiryDateValid")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(org.hamcrest.Matchers.hasItem("Expiry date must be in the future")));
  }

  @Test
  void createPayment_InvalidCurrency_returnsBadRequest() throws Exception {
    String json = """
        {
        "card_number": "2222405343241234",
         "expiry_month": 10,
          "expiry_year": 2029,
          "currency": "INR",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("currency")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(org.hamcrest.Matchers.hasItem("Currency must be one of: USD, GBP, EUR")));
  }

  @Test
  void createPayment_InvalidAmount_returnsBadRequest() throws Exception {
    String json = """
        {
        "card_number": "2222405343241234",
         "expiry_month": 10,
          "expiry_year": 2029,
          "currency": "USD",
          "amount": -10.00,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("amount")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(org.hamcrest.Matchers.hasItem("Amount must be a positive integer")));
  }

  @Test
  void createPayment_InvalidCVV_returnsBadRequest() throws Exception {
    String json = """
        {
        "card_number": "2222405343241234",
         "expiry_month": 10,
          "expiry_year": 2029,
          "currency": "USD",
          "amount": 1000,
          "cvv": "1A3"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("cvv")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(org.hamcrest.Matchers.hasItem("CVV must contain only numeric characters")));
  }

  @Test
  void createPayment_InvalidCVVLength_returnsBadRequest() throws Exception {
    String json = """
        {
        "card_number": "2222405343241234",
         "expiry_month": 10,
          "expiry_year": 2029,
          "currency": "USD",
          "amount": 1000,
          "cvv": "12345"
        }
        """;

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", UUID.randomUUID())
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.[*].field")
            .value(org.hamcrest.Matchers.hasItem("cvv")))
        .andExpect(jsonPath("$.errors.[*].message")
            .value(org.hamcrest.Matchers.hasItem("CVV must be 3 or 4 digits")));
  }


  @Test
  void sameKey_shouldNotDuplicateAndReturnSameResponse() throws Exception {
    String idemKey = "6e1a273e-54e4-4790-a927-0ea054c36023";
    String body = """
        {
          "card_number": "2222405343248877",
          "expiry_month": 11,
          "expiry_year": 2026,
          "currency": "GBP",
          "amount": 10000,
          "cvv": "123"
        }
            """;
    Payment payment = new Payment.Builder().id(UUID.randomUUID())
        .amount(10).currency("USD").status(PaymentStatus.AUTHORIZED)
        .expiryMonth(12).expiryYear(2024).cardNumber("123456789011112").build();
    when(paymentGatewayService.processPayment(any())).thenReturn(payment);
    when(idempotencyService.get(any())).thenReturn(Optional.empty());
    // 1st call
    MvcResult first = mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", idemKey)
            .content(body))
        .andExpect(status().isCreated())
        .andReturn();

    String firstResponse = first.getResponse().getContentAsString();

    // 2nd call with SAME key + body
    MvcResult second = mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", idemKey)
            .content(body))
        .andExpect(status().isCreated())        // or .isConflict() depending on design
        .andReturn();

    String secondResponse = second.getResponse().getContentAsString();

    // Assert: same response, no extra side‑effect
    assertThat(secondResponse).isEqualTo(firstResponse);
  }
}
