package com.mailnest.newsletters;

import com.mailnest.auth.AuthService;
import com.mailnest.error.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewsletterController {

  private final NewsletterService service;
  private final AuthService authService;

  public NewsletterController(NewsletterService service, AuthService authService) {
    this.service = service;
    this.authService = authService;
  }

  @PostMapping("/newsletters")
  public ResponseEntity<Void> publishNewsletter(
      @Valid @RequestBody NewsletterRequest request, HttpServletRequest httpRequest) {
    Credentials credentials = basicAuthentication(httpRequest);
    authService.validateCredentials(credentials);

    service.publish(request);

    return ResponseEntity.ok().build();
  }

  private Credentials basicAuthentication(HttpServletRequest request) {
    String headerValue = request.getHeader("Authorization");

    if (headerValue == null) {
      throw new UnauthorizedException("The 'Authorization' header was missing.");
    }

    if (!headerValue.startsWith("Basic ")) {
      throw new UnauthorizedException("The authorization scheme was not 'Basic'.");
    }

    String base64Segment = headerValue.substring("Basic ".length());

    byte[] decodedBytes;
    try {
      decodedBytes = Base64.getDecoder().decode(base64Segment);
    } catch (IllegalArgumentException e) {
      throw new UnauthorizedException("Failed to base64-decode 'Basic' credentials.");
    }

    String decodedCredentials = new String(decodedBytes, StandardCharsets.UTF_8);
    String[] parts = decodedCredentials.split(":", 2);

    if (parts.length != 2) {
      throw new UnauthorizedException("A username and password must be provided in 'Basic' auth.");
    }

    return new Credentials(parts[0], parts[1]);
  }
}
