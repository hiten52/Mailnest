package com.mailnest.newsletters;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewsletterController {

  private final NewsletterService service;

  public NewsletterController(NewsletterService service) {
    this.service = service;
  }

  @PostMapping("/newsletters")
  public ResponseEntity<Void> publishNewsletter(@Valid @RequestBody NewsletterRequest request) {
    service.publish(request);

    return ResponseEntity.ok().build();
  }
}
