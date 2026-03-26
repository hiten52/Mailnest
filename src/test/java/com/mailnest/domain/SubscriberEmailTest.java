package com.mailnest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SubscriberEmailTest {

  @Test
  void emptyStringIsRejected() {
    assertThat(SubscriberEmail.parse("")).isEmpty();
  }

  @Test
  void emailMissingAtSymbolIsRejected() {
    assertThat(SubscriberEmail.parse("ursuladomain.com")).isEmpty();
  }

  @Test
  void emailMissingSubjectIsRejected() {
    assertThat(SubscriberEmail.parse("@domain.com")).isEmpty();
  }

  @Test
  void validEmailIsParsedSuccessfully() {
    assertThat(SubscriberEmail.parse("ursula@domain.com")).isPresent();
  }
}
