package com.mailnest.auth;

import com.mailnest.error.UnauthorizedException;
import com.mailnest.newsletters.Credentials;
import com.mailnest.newsletters.User;
import com.mailnest.newsletters.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;

  // Valid bcrypt hash for a dummy password.
  // Used to reduce timing differences between:
  // - unknown username
  // - wrong password
  private static final String DUMMY_PASSWORD_HASH =
      "$2a$10$7EqJtq98hPqEX7fNZaFWoO5R6fX1R9G6xP4G5vYhQ0GQf8n4x3V9K";

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UUID validateCredentials(Credentials credentials) {
    Optional<User> maybeUser = getStoredCredentials(credentials.username());

    String expectedPasswordHash = maybeUser.map(User::getPasswordHash).orElse(DUMMY_PASSWORD_HASH);

    verifyPasswordHash(credentials.password(), expectedPasswordHash);

    if (maybeUser.isEmpty()) {
      throw new UnauthorizedException("Invalid credentials");
    }

    return maybeUser.get().getUserId();
  }

  private Optional<User> getStoredCredentials(String username) {
    return userRepository.findByUsername(username);
  }

  private void verifyPasswordHash(String passwordCandidate, String expectedPasswordHash) {
    boolean valid = BCrypt.checkpw(passwordCandidate, expectedPasswordHash);
    if (!valid) {
      throw new UnauthorizedException("Invalid credentials");
    }
  }
}
