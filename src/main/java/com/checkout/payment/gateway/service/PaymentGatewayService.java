package com.checkout.payment.gateway.service;

import static java.lang.String.valueOf;

import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.AcquiringBankRequest;
import com.checkout.payment.gateway.model.AcquiringBankResponse;
import com.checkout.payment.gateway.model.Payment;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final AcquiringBankClient acquiringBankClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository,
      AcquiringBankClient acquiringBankClient) {
    this.paymentsRepository = paymentsRepository;
    this.acquiringBankClient = acquiringBankClient;
  }

  public Payment getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new PaymentNotFoundException("Invalid ID"));
  }

  public Payment processPayment(Payment paymentRequest) {

    paymentRequest.updateStatus(PaymentStatus.SUBMITTED);
    paymentsRepository.add(paymentRequest);

    var acquiringBankResponse = new AcquiringBankResponse();
    try {
      acquiringBankResponse = acquiringBankClient.processPayment(
          mapToAcquirerRequest(paymentRequest));
      LOG.info("acquiringBankResponse " + acquiringBankResponse);
    } catch (Exception e) {
      LOG.error("Error while processing upstream payment", e);
      throw new EventProcessingException("Error while processing payment");
    }
    paymentRequest.updateAuthorizationCode(
        acquiringBankResponse.getAuthorizationCode());
    paymentRequest.updateStatus(
        acquiringBankResponse.getAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED);

    paymentsRepository.add(paymentRequest);
    return paymentRequest;

  }

  private AcquiringBankRequest mapToAcquirerRequest(Payment payment) {
    return new AcquiringBankRequest(
        valueOf(payment.getCardNumber()),
        payment.getExpiryMonth() + "/" + payment.getExpiryYear(),
        payment.getCurrency(),
        payment.getAmount(),
        payment.getCvv()
    );
  }

  private String getLastFour(String cardNumber) {
    return cardNumber.substring(cardNumber.length() - 4);
  }
}
