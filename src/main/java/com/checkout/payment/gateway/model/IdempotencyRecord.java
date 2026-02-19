package com.checkout.payment.gateway.model;

import java.util.UUID;

public class IdempotencyRecord {
  private UUID key;

  private PostPaymentResponse postPaymentResponse;

  private int statusCode;

  public UUID getKey() {
    return key;
  }

  public void setKey(UUID key) {
    this.key = key;
  }

  public PostPaymentResponse getPostPaymentResponse() {
    return postPaymentResponse;
  }

  public void setPostPaymentResponse(PostPaymentResponse postPaymentResponse) {
    this.postPaymentResponse = postPaymentResponse;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
}
