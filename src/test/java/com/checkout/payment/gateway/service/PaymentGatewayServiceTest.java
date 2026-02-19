package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentProcessingException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.AcquiringBankResponse;
import com.checkout.payment.gateway.model.Payment;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentGatewayServiceTest {
  @Mock
  PaymentsRepository paymentsRepository;

  @Mock
  AcquiringBankClient acquiringBankClient;

  @InjectMocks
  PaymentGatewayService paymentGatewayService;

  @Test
  void getPaymentById_whenFound_returnsPayment() {
    UUID id = UUID.randomUUID();
    Payment payment = new Payment.Builder().id(UUID.randomUUID()).build();


    when(paymentsRepository.get(id)).thenReturn(Optional.of(payment));

    Payment result = paymentGatewayService.getPaymentById(id);

    assertThat(result).isSameAs(payment);
    verify(paymentsRepository).get(id);
  }

  @Test
  void getPaymentById_whenNotFound_throwsPaymentNotFoundException() {
    UUID id = UUID.randomUUID();
    when(paymentsRepository.get(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentGatewayService.getPaymentById(id))
        .isInstanceOf(PaymentNotFoundException.class)
        .hasMessage("Invalid ID");

    verify(paymentsRepository).get(id);
  }

  @Test
  void processPayment_whenAuthorized_updatesStatusAndAuthorizationCode_andPersistsTwice() {
    Payment payment = new Payment.Builder().
    id(UUID.randomUUID()).
    cardNumber("2222405343248877").
    expiryMonth(11).
    expiryYear(2026).
    currency("GBP").
    amount(10000).
    cvv("123").build();

    UUID authCode = UUID.randomUUID();
    AcquiringBankResponse bankResponse = new AcquiringBankResponse(true, authCode);
    when(acquiringBankClient.processPayment(any()))
        .thenReturn(bankResponse);

    Payment result = paymentGatewayService.processPayment(payment);

    // status transitions
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(result.getAuthorizationCode()).isEqualTo(authCode);

    // initial store (SUBMITTED) + final update
    verify(paymentsRepository, times(2)).add(payment);

    // acquiringBankClient called with mapped request
    verify(acquiringBankClient).processPayment(argThat(req ->
        req.getCardNumber().equals("2222405343248877")
            && req.getExpiryDate().equals("11/2026")
            && req.getCurrency().equals("GBP")
            && req.getAmount() == 10000
            && req.getCvv().equals("123")
    ));
  }

  @Test
  void processPayment_whenDeclined_setsDeclinedStatus() {
    Payment payment = new Payment.Builder().id(UUID.randomUUID()).build();

    UUID authCode = UUID.randomUUID();
    when(acquiringBankClient.processPayment(any()))
        .thenReturn(new AcquiringBankResponse(false, authCode));

    Payment result = paymentGatewayService.processPayment(payment);

    assertThat(result.getStatus()).isEqualTo(PaymentStatus.DECLINED);
    assertThat(result.getAuthorizationCode()).isEqualTo(authCode);
  }

  @Test
  void processPayment_whenAcquirerThrows_wrapsInEventProcessingException() {
    Payment payment = new Payment.Builder().id(UUID.randomUUID()).build();

    when(acquiringBankClient.processPayment(any()))
        .thenThrow(new RuntimeException("Upstream error"));

    assertThatThrownBy(() -> paymentGatewayService.processPayment(payment))
        .isInstanceOf(PaymentProcessingException.class)
        .hasMessage("Error while processing payment");

    // still stored once as SUBMITTED before calling acquirer
    verify(paymentsRepository).add(payment);
  }
}
