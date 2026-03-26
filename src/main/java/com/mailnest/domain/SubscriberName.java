package com.mailnest.domain;

import java.util.Optional;

public final class SubscriberName {

  private final String value;

  private SubscriberName(String value) {
    this.value = value;
  }

  public static Optional<SubscriberName> parse(String input) {
    String s = input == null ? "" : input.trim();

    boolean isEmpty = s.isEmpty();
    boolean isTooLong = s.length() > 256;

    char[] forbidden = {'/', '(', ')', '"', '<', '>', '\\', '{', '}'};
    boolean hasForbidden = false;
    for (char c : forbidden) {
      if (s.indexOf(c) >= 0) {
        hasForbidden = true;
        break;
      }
    }

    if (isEmpty || isTooLong || hasForbidden) {
      return Optional.empty();
    }

    return Optional.of(new SubscriberName(s));
  }

  public String asString() {
    return value;
  }
}
