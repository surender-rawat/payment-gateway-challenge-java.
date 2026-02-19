package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public List<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
    List<Map<String, String>> errorList = new ArrayList<>();
    Map<String, String> errorMap = new HashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error -> {
      errorMap.put("field", error.getField());
      errorMap.put("message", error.getDefaultMessage());
      errorList.add(errorMap);
        }
    );

    return errorList;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(PaymentNotFoundException.class)
  public Map<String, String> handleEventProcessingError(PaymentNotFoundException ex) {
    LOG.error("Invalid Id");
    Map<String, String> errors = new HashMap<>();
    errors.put("message","Page not found");
    errors.put("error","invalid id");
    return errors;
  }
}
