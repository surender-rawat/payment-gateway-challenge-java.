package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.YearMonth;

public class PostPaymentRequest implements Serializable {

  @NotNull(message = "Card number is required")
  @Size(min = 14, max = 19, message = "Card number must be between 14 and 19 digits")
  @Pattern(regexp = "\\d+", message = "Card number must contain only numeric characters")
  @JsonProperty("card_number")
  private String cardNumber;
  @NotNull(message = "Expiry month is required")
  @Min(value = 1, message = "Expiry month must be between 1 and 12")
  @Max(value = 12, message = "Expiry month must be between 1 and 12")
  @JsonProperty("expiry_month")
  private Integer expiryMonth;
  @NotNull(message = "Expiry year is required")
  @JsonProperty("expiry_year")
  private Integer expiryYear;
  @NotNull(message = "Currency is required")
  @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
  @Pattern(regexp = "USD|GBP|EUR", message = "Currency must be one of: USD, GBP, EUR")
  private String currency;
  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be a positive integer")
  private int amount;
  @NotNull(message = "CVV is required")
  @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
  @Pattern(regexp = "\\d+", message = "CVV must contain only numeric characters")
  private String cvv;

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public int getExpiryMonth() {
    return expiryMonth;
  }

  public void setExpiryMonth(int expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public int getExpiryYear() {
    return expiryYear;
  }

  public void setExpiryYear(int expiryYear) {
    this.expiryYear = expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getCvv() {
    return cvv;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }

  @AssertTrue(message = "Expiry date must be in the future")
  public boolean isExpiryDateValid() {
    if (expiryYear == null || expiryMonth == null || expiryMonth <=  0 ||  expiryMonth >  12) {
      return true;
    }
    YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
    return expiry.isAfter(YearMonth.now());
  }

  private String maskCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.length() < 4) {
      return "****";
    }
    return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
  }

  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumberLastFour=" + maskCardNumber(cardNumber)  +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }
}
