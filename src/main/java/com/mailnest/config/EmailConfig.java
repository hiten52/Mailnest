package com.mailnest.config;

import com.mailnest.domain.SubscriberEmail;
import com.mailnest.email.EmailClient;
import com.mailnest.email.RestEmailClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfig {

  @Bean
  public EmailClient emailClient(EmailProperties properties) {
    return new RestEmailClient(
        properties.getBaseUrl(),
        SubscriberEmail.parse(properties.getSender()).orElseThrow(),
        properties.getAuthorizationToken(),
        Duration.ofMillis(properties.getTimeoutMillis()));
  }
}
