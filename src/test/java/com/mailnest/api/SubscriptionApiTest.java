package com.mailnest.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mailnest.subscriptions.Subscriber;
import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SubscriptionApiTest {

  @LocalServerPort private int port;

  @Autowired private SubscriberRepository subscriberRepository;
  @Autowired private SubscriptionTokenRepository tokenRepository;

  private TestApiClient api;

  private static final WireMockServer emailServer = new WireMockServer(0);

  @BeforeAll
  static void startWireMock() {
    emailServer.start();
  }

  @AfterAll
  static void stopWireMock() {
    emailServer.stop();
  }

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("app.email.base-url", emailServer::baseUrl);
  }

  @BeforeEach
  void setUp() {
    api = new TestApiClient(port, subscriberRepository, tokenRepository);
    api.clearSubscribers();
    emailServer.resetAll();
  }

  private void mockSuccessfulEmailDelivery() {
    emailServer.stubFor(post(urlEqualTo("/email")).willReturn(aResponse().withStatus(200)));
  }

  private String extractLink(String text) {
    Pattern pattern = Pattern.compile("https?://[^\\s\"'<>]+");
    Matcher matcher = pattern.matcher(text);

    if (!matcher.find()) {
      throw new AssertionError("Expected exactly one link, found none.");
    }

    String link = matcher.group();

    if (matcher.find()) {
      throw new AssertionError("Expected exactly one link, found more than one.");
    }

    URI.create(link);
    return link;
  }

  @Test
  void subscribeReturns200ForValidFormData() throws Exception {
    mockSuccessfulEmailDelivery();

    String body = "name=le%20guin&email=ursula_le_guin%40gmail.com";

    var response = api.postSubscriptions(body);

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  void subscribePersistsTheNewSubscriber() throws Exception {
    mockSuccessfulEmailDelivery();

    String body = "name=le%20guin&email=ursula_le_guin%40gmail.com";

    api.postSubscriptions(body);

    var savedSubscribers = api.getSavedSubscribers();
    assertThat(savedSubscribers).hasSize(1);

    Subscriber savedSubscriber = savedSubscribers.get(0);
    assertThat(savedSubscriber.getEmail()).isEqualTo("ursula_le_guin@gmail.com");
    assertThat(savedSubscriber.getName()).isEqualTo("le guin");
    assertThat(savedSubscriber.getStatus()).isEqualTo("pending_confirmation");
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

  @Test
  void subscribeSendsConfirmationEmailForValidData() throws Exception {
    mockSuccessfulEmailDelivery();

    String body = "name=test&email=test@gmail.com";

    var response = api.postSubscriptions(body);

    assertThat(response.statusCode()).isEqualTo(200);

    emailServer.verify(1, postRequestedFor(urlEqualTo("/email")));
  }

  @Test
  void subscribeSendsConfirmationEmailWithALink() throws Exception {
    mockSuccessfulEmailDelivery();

    String body = "name=le%20guin&email=ursula_le_guin%40gmail.com";

    var response = api.postSubscriptions(body);
    assertThat(response.statusCode()).isEqualTo(200);

    var requests = emailServer.getAllServeEvents().get(0);
    var confirmationLinks = api.getConfirmationLinks(requests);

    assertThat(confirmationLinks.html()).isEqualTo(confirmationLinks.plainText());
  }
}
