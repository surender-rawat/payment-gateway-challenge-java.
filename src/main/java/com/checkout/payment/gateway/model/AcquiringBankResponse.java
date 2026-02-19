package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class AcquiringBankResponse {
  @JsonProperty("authorized")
  private boolean authorized;

  @JsonProperty("authorization_code")
  private UUID authorizationCode;

  public AcquiringBankResponse() {}

  public AcquiringBankResponse(boolean authorized, UUID authorizationCode) {
    this.authorized = authorized;
    this.authorizationCode = authorizationCode;
  }

  public boolean getAuthorized() {
    return authorized;
  }

  public void setAuthorized(boolean authorized) {
    this.authorized = authorized;
  }

  public UUID getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAuthorizationCode(UUID authorizationCode) {
    this.authorizationCode = authorizationCode;
  }
}
