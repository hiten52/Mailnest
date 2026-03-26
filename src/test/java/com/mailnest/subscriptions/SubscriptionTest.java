package com.mailnest.subscriptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.test.context.ActiveProfiles("test")
class SubscriptionTest {

  @LocalServerPort private int port;

  @Autowired private SubscriberRepository subscriberRepository;

  @BeforeEach
  void cleanDatabase() {
    subscriberRepository.deleteAll();
  }

  private final HttpClient client = HttpClient.newHttpClient();

  @Test
  void subscribeReturns200ForValidFormData() throws IOException, InterruptedException {
    String body = "name=le%20guin&email=ursula_le_guin%40gmail.com";

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/subscriptions"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);

    var savedSubscribers = subscriberRepository.findAll();
    assertThat(savedSubscribers).hasSize(1);

    Subscriber savedSubscriber = savedSubscribers.get(0);
    assertThat(savedSubscriber.getEmail()).isEqualTo("ursula_le_guin@gmail.com");
    assertThat(savedSubscriber.getName()).isEqualTo("le guin");
  }

  @Test
  void subscribeReturns400WhenDataIsMissing() throws IOException, InterruptedException {
    String[][] testCases = {
      {"name=le%20guin", "missing the email"},
      {"email=ursula_le_guin%40gmail.com", "missing the name"},
      {"", "missing both name and email"}
    };

    for (String[] testCase : testCases) {
      String invalidBody = testCase[0];
      String errorMessage = testCase[1];

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:" + port + "/subscriptions"))
              .header("Content-Type", "application/x-www-form-urlencoded")
              .POST(HttpRequest.BodyPublishers.ofString(invalidBody))
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode())
          .withFailMessage(
              "The API did not fail with 400 Bad Request when the payload was %s.", errorMessage)
          .isEqualTo(400);
    }
  }

  @Test
  void subscribeReturns400WhenFieldsArePresentButInvalid()
      throws IOException, InterruptedException {
    String[][] testCases = {
      {"name=&email=ursula_le_guin%40gmail.com", "empty name"},
      {"name=   &email=ursula_le_guin%40gmail.com", "blank name"},
      {"name=le%20guin&email=", "empty email"},
      {"name=le%20guin&email=not-an-email", "invalid email"},
      {"name=%3Cbad%3E&email=ursula_le_guin%40gmail.com", "name with forbidden characters"}
    };

    for (String[] testCase : testCases) {
      String invalidBody = testCase[0];
      String description = testCase[1];

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:" + port + "/subscriptions"))
              .header("Content-Type", "application/x-www-form-urlencoded")
              .POST(HttpRequest.BodyPublishers.ofString(invalidBody))
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode())
          .withFailMessage(
              "The API did not return 400 Bad Request when the payload was %s.", description)
          .isEqualTo(400);
    }
  }
}
