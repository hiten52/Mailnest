package com.mailnest.subscriptions;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscriber {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(name = "subscribed_at", nullable = false)
  private OffsetDateTime subscribedAt;

  @Column private String status;

  public Subscriber() {}

  public Subscriber(
      UUID id, String email, String name, OffsetDateTime subscribedAt, String status) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.subscribedAt = subscribedAt;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OffsetDateTime getSubscribedAt() {
    return subscribedAt;
  }

  public void setSubscribedAt(OffsetDateTime subscribedAt) {
    this.subscribedAt = subscribedAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
