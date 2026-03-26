package com.mailnest.subscriptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionConfirmationController {

  @GetMapping("/subscriptions/confirm")
  public ResponseEntity<Void> confirm(@RequestParam("token") String token) {
    return ResponseEntity.ok().build();
  }
}
