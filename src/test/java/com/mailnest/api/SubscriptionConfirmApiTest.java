package com.mailnest.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
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
class SubscriptionConfirmApiTest {

  static final WireMockServer emailServer = new WireMockServer(0);

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
  void confirmationsWithoutTokenAreRejectedWith400() throws Exception {
    var response = api.getSubscriptionConfirmation();

    assertThat(response.statusCode()).isEqualTo(400);
  }

  @Test
  void theLinkReturnedBySubscribeReturns200IfCalled() throws Exception {
    emailServer.stubFor(post(urlEqualTo("/email")).willReturn(aResponse().withStatus(200)));

    String body = "name=le%20guin&email=ursula_le_guin%40gmail.com";

    var subscribeResponse = api.postSubscriptions(body);
    assertThat(subscribeResponse.statusCode()).isEqualTo(200);

    var requests = emailServer.getAllServeEvents().get(0);
    var confirmationLinks = api.getConfirmationLinks(requests);

    var response = api.getSubscriptionConfirmation(confirmationLinks.html());

    assertThat(response.statusCode()).isEqualTo(200);
  }
}
