package com.mailnest.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mailnest.newsletters.UserRepository;
import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

  @Autowired private UserRepository userRepository;

  private TestApiClient api;

  @BeforeEach
  void setUp() {
    api = new TestApiClient(port, subscriberRepository, tokenRepository, userRepository);
    api.clearSubscribers();
    emailServer.resetAll();
  }

  @Test
  void you_must_be_logged_in_to_see_the_newsletter_form() throws Exception {
    String html = api.getNewsletterFormHtml();

    assertThat(html).contains("<title>Login</title>");
  }

  @Test
  void you_must_be_logged_in_to_publish_a_newsletter() throws Exception {
    String body =
        "title="
            + encode("Newsletter title")
            + "&textContent="
            + encode("Plain text")
            + "&htmlContent="
            + encode("<p>HTML</p>");

    var response = api.postNewsletter(body);

    assertThat(response.body()).contains("<title>Login</title>");
  }

  @Test
  void newslettersAreNotDeliveredToUnconfirmedSubscribers() throws Exception {
    createUnconfirmedSubscriber();
    login();

    emailServer.resetRequests();

    String body =
        "title="
            + encode("Newsletter title")
            + "&textContent="
            + encode("Newsletter body as plain text")
            + "&htmlContent="
            + encode("<p>Newsletter body as HTML</p>");

    var response = api.postNewsletter(body);

    assertThat(response.body()).contains("Newsletter published successfully!");
    emailServer.verify(0, postRequestedFor(urlEqualTo("/email")));
  }

  @Test
  void newslettersAreDeliveredToConfirmedSubscribers() throws Exception {
    createConfirmedSubscriber();
    login();

    emailServer.resetRequests();
    emailServer.stubFor(post(urlEqualTo("/email")).willReturn(aResponse().withStatus(200)));

    String body =
        "title="
            + encode("Newsletter title")
            + "&textContent="
            + encode("Newsletter body as plain text")
            + "&htmlContent="
            + encode("<p>Newsletter body as HTML</p>");

    var response = api.postNewsletter(body);

    assertThat(response.body()).contains("Newsletter published successfully!");
    emailServer.verify(1, postRequestedFor(urlEqualTo("/email")));
  }

  @Test
  void newsletterReturnsErrorForMissingTitle() throws Exception {
    login();

    String body =
        "title=&textContent="
            + encode("Newsletter body")
            + "&htmlContent="
            + encode("<p>Newsletter</p>");

    var response = api.postNewsletter(body);

    assertThat(response.body()).contains("Title is required.");
  }

  private void login() throws Exception {
    api.postLogin(
        "username=" + encode(api.testUser.username) + "&password=" + encode(api.testUser.password));
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

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
