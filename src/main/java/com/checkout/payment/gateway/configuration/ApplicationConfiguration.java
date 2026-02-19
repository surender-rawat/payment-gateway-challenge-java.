package com.checkout.payment.gateway.configuration;

import java.time.Duration;
import com.checkout.payment.gateway.client.AcquiringBankClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@Configuration
public class  ApplicationConfiguration {
  @Autowired
  private Environment env;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofMillis(10000))
        .setReadTimeout(Duration.ofMillis(10000))
        .build();
  }

  @Bean
  public AcquiringBankClient acquiringBankClient() {
    return new AcquiringBankClient(
        env.getProperty("AcquiringBank.baseUri"),
        env.getProperty("AcquiringBank.port"));
  }
}
