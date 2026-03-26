package com.mailnest.subscriptions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "subscription_tokens")
public class SubscriptionToken {

  @Id
  @Column(name = "subscription_token", nullable = false, updatable = false)
  private String subscriptionToken;

  @Column(name = "subscriber_id", nullable = false)
  private UUID subscriberId;

  public SubscriptionToken() {}

  public SubscriptionToken(String subscriptionToken, UUID subscriberId) {
    this.subscriptionToken = subscriptionToken;
    this.subscriberId = subscriberId;
  }

  public String getSubscriptionToken() {
    return subscriptionToken;
  }

  public void setSubscriptionToken(String subscriptionToken) {
    this.subscriptionToken = subscriptionToken;
  }

  public UUID getSubscriberId() {
    return subscriberId;
  }

  public void setSubscriberId(UUID subscriberId) {
    this.subscriberId = subscriberId;
  }
}
