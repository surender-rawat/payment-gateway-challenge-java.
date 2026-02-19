package com.checkout.payment.gateway.client;

import org.springframework.web.client.RestTemplate;

public abstract class BaseClient {

  protected final RestTemplate restTemplate;
  protected final String baseUri;
  protected final String port;

  public BaseClient(String baseUri, String port) {
    this.restTemplate = new RestTemplate();
    this.baseUri = baseUri;
    this.port = port;
  }

  public <T> Object get(
      String path,
      Class<T> responseType,
      Object... uriVariables) {
    return restTemplate.getForObject(baseUri + ":" + port + path, responseType, uriVariables);
  }

  public <T> Object post(
      String path,
      Object request,
      Class<T> responseType,
      Object... uriVariables) {
    return restTemplate.postForObject(
        baseUri + ":" + port + path, request, responseType, uriVariables);
  }

  public void put(
      String path,
      Object request,
      Object... uriVariables) {
    restTemplate.put(baseUri + ":" + port + path, request, uriVariables);
  }

  public void delete(
      String path,
      Object request,
      Object... uriVariables) {
    restTemplate.delete(baseUri + ":" + port + path, request, uriVariables);
  }

}