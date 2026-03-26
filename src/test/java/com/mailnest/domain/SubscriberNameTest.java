package com.mailnest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SubscriberNameTest {

  @Test
  void a256CharacterLongNameIsValid() {
    String name = "a".repeat(256);

    assertThat(SubscriberName.parse(name)).isPresent();
  }

  @Test
  void aNameLongerThan256CharactersIsRejected() {
    String name = "a".repeat(257);

    assertThat(SubscriberName.parse(name)).isEmpty();
  }

  @Test
  void whitespaceOnlyNamesAreRejected() {
    String name = "   ";

    assertThat(SubscriberName.parse(name)).isEmpty();
  }

  @Test
  void emptyStringIsRejected() {
    String name = "";

    assertThat(SubscriberName.parse(name)).isEmpty();
  }

  @Test
  void namesContainingInvalidCharactersAreRejected() {
    char[] invalidCharacters = {'/', '(', ')', '"', '<', '>', '\\', '{', '}'};

    for (char invalidCharacter : invalidCharacters) {
      String name = String.valueOf(invalidCharacter);
      assertThat(SubscriberName.parse(name)).isEmpty();
    }
  }

  @Test
  void aValidNameIsParsedSuccessfully() {
    String name = "Ursula Le Guin";

    assertThat(SubscriberName.parse(name)).isPresent();
  }
}
