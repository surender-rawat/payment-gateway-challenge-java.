package com.checkout.payment.gateway;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.model.AcquiringBankResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PaymentGatewayIT {

  @Autowired
  private MockMvc mvc;

  @MockBean
  AcquiringBankClient acquiringBankClient;


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

    when(acquiringBankClient.processPayment(any())).thenReturn(new AcquiringBankResponse(true,
        UUID.randomUUID()));

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
        .andExpect(status().isCreated())
        .andReturn();

    String secondResponse = second.getResponse().getContentAsString();
    assertThat(secondResponse).isEqualTo(firstResponse);
  }

  @Test
  void differentKey_shouldReturnDifferentResponse() throws Exception {
    String idemKeyFirst = "7e1a273e-54e4-4790-a927-0ea054c36023";
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

    when(acquiringBankClient.processPayment(any())).thenReturn(new AcquiringBankResponse(true,
        UUID.randomUUID()));

    // 1st call
    MvcResult first = mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", idemKeyFirst)
            .content(body))
        .andExpect(status().isCreated())
        .andReturn();

    String firstResponse = first.getResponse().getContentAsString();

    // 2nd call with SAME key + body
    String idemKeySecond = "8e1a273e-54e4-4790-a927-0ea054c36023";
    MvcResult second = mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", idemKeySecond)
            .content(body))
        .andExpect(status().isCreated())
        .andReturn();

    String secondResponse = second.getResponse().getContentAsString();
    assertThat(secondResponse).isNotEqualTo(firstResponse);
  }

  @Test
  void shouldReturnResponseAsMaskWithRelevantField() throws Exception {
    String idemKeyFirst = "7e1a273e-54e4-4790-a927-0ea054c36023";
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

    when(acquiringBankClient.processPayment(any())).thenReturn(new AcquiringBankResponse(true,
        UUID.randomUUID()));

    // 1st call
    MvcResult first = mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", idemKeyFirst)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.cardNumberLastFourDigit").value("8877"))
        .andExpect(jsonPath("$.expiry_month").doesNotExist())
        .andExpect(jsonPath("$.expiry_year").doesNotExist())
        .andExpect(jsonPath("$.cvv").doesNotExist())
        .andExpect(jsonPath("$.currency").value("GBP"))
        .andExpect(jsonPath("$.amount").value(10000))
        .andReturn();


    String firstResponse = first.getResponse().getContentAsString();
    assertThat(firstResponse).doesNotContain("2222405343248877");

  }


}
