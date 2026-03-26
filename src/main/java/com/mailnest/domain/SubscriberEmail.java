package com.mailnest.domain;

import java.util.Optional;
import org.apache.commons.validator.routines.EmailValidator;

public final class SubscriberEmail {

  private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance(false, false);

  private final String value;

  private SubscriberEmail(String value) {
    this.value = value;
  }

  public static Optional<SubscriberEmail> parse(String input) {
    if (input == null) {
      return Optional.empty();
    }

    String s = input.trim();
    if (!EMAIL_VALIDATOR.isValid(s)) {
      return Optional.empty();
    }

    return Optional.of(new SubscriberEmail(s));
  }

  public String asString() {
    return value;
  }
}
