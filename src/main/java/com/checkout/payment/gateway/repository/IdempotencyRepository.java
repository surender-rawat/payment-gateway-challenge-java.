package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.model.IdempotencyRecord;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.UUID;

@Repository
public class IdempotencyRepository {
  private final HashMap<UUID, IdempotencyRecord> idempotencyRepo = new HashMap<>();

  public IdempotencyRecord findById(UUID key) {
    return idempotencyRepo.get(key);
  }

  public void save(IdempotencyRecord  idempotencyRecord) {
    idempotencyRepo.put(idempotencyRecord.getKey(),idempotencyRecord);
  }

}
