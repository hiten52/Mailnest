package com.mailnest.subscriptions;

import com.mailnest.domain.NewSubscriber;
import jakarta.validation.Valid;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {

  private final SubscriptionService service;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "Spring-managed service dependency is injected via constructor and not exposed.")
  public SubscriptionController(SubscriptionService service) {
    this.service = service;
  }

  @PostMapping("/subscriptions")
  public ResponseEntity<Void> subscribe(@Valid @ModelAttribute SubscriptionRequest request) {
    NewSubscriber newSubscriber = SubscriberMapper.from(request);
    service.subscribe(newSubscriber);

    return ResponseEntity.ok().build();
  }
}
