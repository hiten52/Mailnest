package com.mailnest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmacSecret {

  private final String secret;

  public HmacSecret(@Value("${app.hmac-secret}") String secret) {
    this.secret = secret;
  }

  public String getSecret() {
    return secret;
  }
}
