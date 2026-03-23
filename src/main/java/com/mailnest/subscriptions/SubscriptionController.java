package com.mailnest.subscriptions;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {

  @PostMapping("/subscriptions")
  public ResponseEntity<Void> subscribe(@Valid @ModelAttribute SubscriptionRequest request) {
    return ResponseEntity.ok().build();
  }
}
