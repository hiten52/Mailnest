package com.mailnest.subscriptions;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {

  private final SubscriptionService service;

  public SubscriptionController(SubscriptionService service) {
    this.service = service;
  }

  @PostMapping("/subscriptions")
  public ResponseEntity<Void> subscribe(@Valid @ModelAttribute SubscriptionRequest request) {
    service.subscribe(request);
    return ResponseEntity.ok().build();
  }
}
