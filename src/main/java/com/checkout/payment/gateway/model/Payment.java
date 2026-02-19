package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.UUID;


public class Payment {

  private final UUID id;
  private  PaymentStatus status;
  private final String cardNumber;
  private final int expiryMonth;
  private final int expiryYear;
  private final String currency;
  private final int amount;
  private final String cvv;
  private  UUID authorizationCode;

  private Payment(Builder builder) {
    this.id = builder.id;
    this.status = builder.status;
    this.cardNumber = builder.cardNumber;
    this.expiryMonth = builder.expiryMonth;
    this.expiryYear = builder.expiryYear;
    this.currency = builder.currency;
    this.amount = builder.amount;
    this.cvv = builder.cvv;
  }

  // Getters
  public UUID getId() {
    return id;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public String getCardNumber() {
    return cardNumber;
  }

  public int getExpiryMonth() {
    return expiryMonth;
  }

  public int getExpiryYear() {
    return expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public int getAmount() {
    return amount;
  }

  public String getCvv() {
    return cvv;
  }

  public UUID getAuthorizationCode() {
    return authorizationCode;
  }

  public void updateStatus(PaymentStatus newStatus) {
    this.status = newStatus;
  }

  public void updateAuthorizationCode(UUID newAuthorizationCode) {
    this.authorizationCode = newAuthorizationCode;
  }

  // Builder class
  public static class Builder {
    private UUID id;
    private PaymentStatus status;
    private String cardNumber;
    private int expiryMonth;
    private int expiryYear;
    private String currency;
    private int amount;
    private String cvv;
    private UUID authorizationCode;

    public Builder id(UUID id) {
      this.id = id;
      return this;
    }

    public Builder status(PaymentStatus status) {
      this.status = status;
      return this;
    }

    public Builder cardNumber(String cardNumber) {
      this.cardNumber = cardNumber;
      return this;
    }

    public Builder expiryMonth(int expiryMonth) {
      this.expiryMonth = expiryMonth;
      return this;
    }

    public Builder expiryYear(int expiryYear) {
      this.expiryYear = expiryYear;
      return this;
    }

    public Builder currency(String currency) {
      this.currency = currency;
      return this;
    }

    public Builder amount(int amount) {
      this.amount = amount;
      return this;
    }

    public Builder cvv(String cvv) {
      this.cvv = cvv;
      return this;
    }

    public Builder authorizationCode(UUID authorizationCode) {
      this.authorizationCode = authorizationCode;
      return this;
    }

    public Payment build() {
      return new Payment(this);
    }
  }
}