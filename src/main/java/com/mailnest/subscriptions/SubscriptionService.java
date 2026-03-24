package com.mailnest.subscriptions;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

  private final SubscriberRepository repository;

  public SubscriptionService(SubscriberRepository repository) {
    this.repository = repository;
  }

  public void subscribe(SubscriptionRequest request) {
    Subscriber s = new Subscriber();
    s.setId(UUID.randomUUID());
    s.setEmail(request.getEmail());
    s.setName(request.getName());
    s.setSubscribedAt(OffsetDateTime.now());

    repository.save(s);
  }
}
