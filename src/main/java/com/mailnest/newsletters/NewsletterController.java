package com.mailnest.newsletters;

import com.mailnest.error.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewsletterController {

  private final NewsletterService service;
  private final UserRepository userRepository;

  // Precomputed bcrypt hash for a dummy password.
  // Used to equalize work when the username does not exist.
  // Raw password used to generate it can be anything, e.g.
  // "not-the-real-password".
  private static final String DUMMY_PASSWORD_HASH =
      "$2a$10$7EqJtq98hPqEX7fNZaFWoO5R6fX1R9G6xP4G5vYhQ0GQf8n4x3V9K";

  public NewsletterController(NewsletterService service, UserRepository userRepository) {
    this.service = service;
    this.userRepository = userRepository;
  }

  @PostMapping("/newsletters")
  public ResponseEntity<Void> publishNewsletter(
      @Valid @RequestBody NewsletterRequest request, HttpServletRequest httpRequest) {
    Credentials credentials = basicAuthentication(httpRequest);
    validateCredentials(credentials);

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

  private void validateCredentials(Credentials credentials) {
    Optional<User> maybeUser = userRepository.findByUsername(credentials.username());

    String expectedPasswordHash = maybeUser.map(User::getPasswordHash).orElse(DUMMY_PASSWORD_HASH);

    boolean valid = BCrypt.checkpw(credentials.password(), expectedPasswordHash);

    if (maybeUser.isEmpty() || !valid) {
      throw new UnauthorizedException("Invalid username or password.");
    }
  }
}
