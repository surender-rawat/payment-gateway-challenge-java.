package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.IdempotencyRecord;
import com.checkout.payment.gateway.model.Payment;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.service.IdempotencyService;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.Optional;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/payments")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;
  private final IdempotencyService idempotencyService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService,
      IdempotencyService idempotencyService) {
    this.paymentGatewayService = paymentGatewayService;
    this.idempotencyService = idempotencyService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<PostPaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    Payment payment = paymentGatewayService.getPaymentById(id);
    return new ResponseEntity<>(toPostPaymentResponse(payment), HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<PostPaymentResponse> processPayment(
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody PostPaymentRequest paymentRequest) {
    // 1. Check for existing response
    Optional<IdempotencyRecord> existing = idempotencyService.get(idempotencyKey);
    if (existing.isPresent()) {
      IdempotencyRecord r = existing.get();
      return new ResponseEntity<>(r.getPostPaymentResponse(),HttpStatus.valueOf(r.getStatusCode()));
    }
    Payment paymentResponse = paymentGatewayService.processPayment(toPayment(paymentRequest));
    PostPaymentResponse postPaymentResponse = toPostPaymentResponse(paymentResponse);
    idempotencyService.save(idempotencyKey,HttpStatus.CREATED.value(), postPaymentResponse);
    return new ResponseEntity<>(postPaymentResponse, HttpStatus.CREATED);
  }

  private PostPaymentResponse toPostPaymentResponse(Payment payment){
    PostPaymentResponse response= new PostPaymentResponse();
    response.setId(payment.getId());
    response.setCardNumberLastFourDigit(Integer.parseInt(payment.getCardNumber().substring(payment.getCardNumber().length()-4)));
    response.setAmount(payment.getAmount());
    response.setCurrency(payment.getCurrency());
    response.setStatus(payment.getStatus());
    response.setExpiryMonth(payment.getExpiryMonth());
    response.setExpiryYear(payment.getExpiryYear());
    return response;
  }

  private Payment toPayment(PostPaymentRequest request){
      return new Payment.Builder().id(UUID.randomUUID())
        .cardNumber(request.getCardNumber())
        .expiryMonth(request.getExpiryMonth())
        .expiryYear(request.getExpiryYear())
        .currency(request.getCurrency())
        .amount(request.getAmount())
        .cvv(request.getCvv())
        .build();
  }
}
