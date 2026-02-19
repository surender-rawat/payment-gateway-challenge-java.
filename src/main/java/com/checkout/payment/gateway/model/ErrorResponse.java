package com.checkout.payment.gateway.model;

import java.util.List;
import java.util.Map;

public class ErrorResponse {
  private  String status;

  private List<Map<String,String>> errors;

  public ErrorResponse(String status, List<Map<String, String>> errors) {
    this.status = status;
    this.errors = errors;
  }

  public String getStatus() {
    return status;
  }

  public List<Map<String, String>> getErrors() {
    return errors;
  }
}
