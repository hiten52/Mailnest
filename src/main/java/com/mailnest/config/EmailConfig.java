package com.mailnest.config;

import com.mailnest.domain.SubscriberEmail;
import com.mailnest.email.EmailClient;
import com.mailnest.email.RestEmailClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

  @Bean
  public EmailClient emailClient() {
    return new RestEmailClient(
        "http://localhost:8081",
        SubscriberEmail.parse("test@gmail.com").orElseThrow(),
        "my-secret-token",
        Duration.ofSeconds(10));
  }
}
