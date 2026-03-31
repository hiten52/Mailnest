package com.mailnest.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
import java.util.Map;
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
class NewsletterApiTest {

  private static final WireMockServer emailServer = new WireMockServer(0);
  private static final ObjectMapper objectMapper = new ObjectMapper();

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
    registry.add("app.email.sender", () -> "test@gmail.com");
    registry.add("app.email.authorization-token", () -> "my-secret-token");
    registry.add("app.email.timeout-millis", () -> 10000);
    registry.add("app.base-url", () -> "http://localhost");
  }

  @LocalServerPort private int port;

  @Autowired private SubscriberRepository subscriberRepository;

  @Autowired private SubscriptionTokenRepository tokenRepository;

  private TestApiClient api;

  @BeforeEach
  void setUp() {
    api = new TestApiClient(port, subscriberRepository, tokenRepository);
    api.clearSubscribers();
    emailServer.resetAll();
  }

  @Test
  void newslettersAreNotDeliveredToUnconfirmedSubscribers() throws Exception {
    createUnconfirmedSubscriber();

    emailServer.resetRequests();

    String newsletterRequestBody =
        objectMapper.writeValueAsString(
            Map.of(
                "title",
                "Newsletter title",
                "content",
                Map.of(
                    "text", "Newsletter body as plain text",
                    "html", "<p>Newsletter body as HTML</p>")));

    var response = api.postNewsletter(newsletterRequestBody);

    assertThat(response.statusCode()).isEqualTo(200);
    emailServer.verify(0, postRequestedFor(urlEqualTo("/email")));
  }

  @Test
  void newslettersAreDeliveredToConfirmedSubscribers() throws Exception {
    createConfirmedSubscriber();

    emailServer.resetRequests();

    emailServer.stubFor(post(urlEqualTo("/email")).willReturn(aResponse().withStatus(200)));

    String newsletterRequestBody =
        objectMapper.writeValueAsString(
            Map.of(
                "title",
                "Newsletter title",
                "content",
                Map.of(
                    "text", "Newsletter body as plain text",
                    "html", "<p>Newsletter body as HTML</p>")));

    var response = api.postNewsletter(newsletterRequestBody);

    assertThat(response.statusCode()).isEqualTo(200);
    emailServer.verify(1, postRequestedFor(urlEqualTo("/email")));
  }

  private TestApiClient.ConfirmationLinks createUnconfirmedSubscriber() throws Exception {
    emailServer.stubFor(post(urlEqualTo("/email")).willReturn(aResponse().withStatus(200)));

    var response = api.postSubscriptions("name=le%20guin&email=ursula_le_guin%40gmail.com");

    assertThat(response.statusCode()).isEqualTo(200);

    var emailRequest =
        emailServer.getAllServeEvents().get(emailServer.getAllServeEvents().size() - 1);

    return api.getConfirmationLinks(emailRequest);
  }

  private void createConfirmedSubscriber() throws Exception {
    var confirmationLinks = createUnconfirmedSubscriber();

    var response = api.getSubscriptionConfirmation(confirmationLinks.html());

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  void newslettersReturns400ForInvalidData() throws Exception {

    var testCases =
        new Object[] {
          Map.of(
              "content",
              Map.of(
                  "text", "Newsletter body",
                  "html", "<p>Newsletter</p>")),
          Map.of("title", "Newsletter!")
        };

    for (Object invalidBody : testCases) {
      String json = new ObjectMapper().writeValueAsString(invalidBody);

      var response = api.postNewsletter(json);

      assertThat(response.statusCode()).isEqualTo(400);
    }
  }
}
