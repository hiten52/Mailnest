package com.mailnest.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.mailnest.subscriptions.Subscriber;
import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SubscriptionApiTest {

  @LocalServerPort private int port;

  @Autowired private SubscriberRepository subscriberRepository;
  @Autowired private SubscriptionTokenRepository tokenRepository;

  private TestApiClient api;

  @BeforeEach
  void setUp() {
    api = new TestApiClient(port, subscriberRepository, tokenRepository);
    api.clearSubscribers();
  }

  @Test
  void subscribeReturns200ForValidFormData() throws Exception {
    String body = "name=le%20guin&email=ursula_le_guin%40gmail.com";

    var response = api.postSubscriptions(body);

    assertThat(response.statusCode()).isEqualTo(200);

    var savedSubscribers = api.getSavedSubscribers();
    assertThat(savedSubscribers).hasSize(1);

    Subscriber savedSubscriber = savedSubscribers.get(0);
    assertThat(savedSubscriber.getEmail()).isEqualTo("ursula_le_guin@gmail.com");
    assertThat(savedSubscriber.getName()).isEqualTo("le guin");
  }

  @Test
  void subscribeReturns400WhenDataIsMissing() throws Exception {
    String[][] testCases = {
      {"name=le%20guin", "missing the email"},
      {"email=ursula_le_guin%40gmail.com", "missing the name"},
      {"", "missing both name and email"}
    };

    for (String[] testCase : testCases) {
      String invalidBody = testCase[0];
      String description = testCase[1];

      var response = api.postSubscriptions(invalidBody);

      assertThat(response.statusCode())
          .withFailMessage(
              "The API did not fail with 400 Bad Request when the payload was %s.", description)
          .isEqualTo(400);
    }
  }

  @Test
  void subscribeReturns400WhenFieldsArePresentButInvalid() throws Exception {
    String[][] testCases = {
      {"name=&email=ursula_le_guin%40gmail.com", "empty name"},
      {"name=%20%20%20&email=ursula_le_guin%40gmail.com", "blank name"},
      {"name=le%20guin&email=", "empty email"},
      {"name=le%20guin&email=not-an-email", "invalid email"},
      {"name=%3Cbad%3E&email=ursula_le_guin%40gmail.com", "name with forbidden characters"}
    };

    for (String[] testCase : testCases) {
      String invalidBody = testCase[0];
      String description = testCase[1];

      var response = api.postSubscriptions(invalidBody);

      assertThat(response.statusCode())
          .withFailMessage(
              "The API did not return 400 Bad Request when the payload was %s.", description)
          .isEqualTo(400);
    }
  }
}
