package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.model.IdempotencyRecord;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.IdempotencyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyService {
  private final IdempotencyRepository repo;

  public IdempotencyService(IdempotencyRepository repo) {
    this.repo = repo;
  }


  public Optional<IdempotencyRecord> get(String key) {
    return Optional.ofNullable(repo.findById(UUID.fromString(key)));
  }

  public void save(String key, int statusCode, PostPaymentResponse body) {
    IdempotencyRecord r = new IdempotencyRecord();
    r.setKey(UUID.fromString(key));
    r.setStatusCode(statusCode);
    r.setPostPaymentResponse(body);
    repo.save(r);
  }
}
