package com.mailnest.subscriptions;

import com.mailnest.error.InvalidTokenException;
import java.util.UUID;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionConfirmationController {

  private final SubscriptionTokenRepository tokenRepository;
  private final SubscriberRepository subscriberRepository;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "Spring-managed service dependency is injected via constructor and not exposed.")
  public SubscriptionConfirmationController(
      SubscriptionTokenRepository tokenRepository, SubscriberRepository subscriberRepository) {
    this.tokenRepository = tokenRepository;
    this.subscriberRepository = subscriberRepository;
  }

  @GetMapping("/subscriptions/confirm")
  public ResponseEntity<Void> confirm(@RequestParam("token") String token) {

    SubscriptionToken tokenEntity =
        tokenRepository
            .findBySubscriptionToken(token)
            .orElseThrow(() -> new InvalidTokenException(token));

    UUID subscriberId = tokenEntity.getSubscriberId();

    Subscriber subscriber = subscriberRepository.findById(subscriberId).orElseThrow();

    subscriber.setStatus("confirmed");
    subscriberRepository.save(subscriber);

    tokenRepository.delete(tokenEntity);

    return ResponseEntity.ok().build();
  }
}
