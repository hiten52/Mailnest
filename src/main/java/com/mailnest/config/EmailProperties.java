package com.mailnest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

  private String baseUrl;
  private String sender;
  private String authorizationToken;
  private long timeoutMillis = 10000;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getAuthorizationToken() {
    return authorizationToken;
  }

  public void setAuthorizationToken(String authorizationToken) {
    this.authorizationToken = authorizationToken;
  }

  public long getTimeoutMillis() {
    return timeoutMillis;
  }

  public void setTimeoutMillis(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }
}
