package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.model.AcquiringBankRequest;
import com.checkout.payment.gateway.model.AcquiringBankResponse;

public class AcquiringBankClient extends BaseClient {

  private static final String PAYMENTS = "/payments";

  public AcquiringBankClient(String baseUri, String port) {
    super(baseUri, port);
  }

  public AcquiringBankResponse processPayment(AcquiringBankRequest request) {
    var response = post(PAYMENTS, request, AcquiringBankResponse.class);
    return (AcquiringBankResponse) response;
  }

}
