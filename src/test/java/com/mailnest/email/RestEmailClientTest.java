package com.mailnest.email;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mailnest.domain.SubscriberEmail;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RestEmailClientTest {

  private WireMockServer wireMockServer;

  @BeforeEach
  void setUp() {
    wireMockServer = new WireMockServer(0);
    wireMockServer.start();
    configureFor("localhost", wireMockServer.port());
  }

  @AfterEach
  void tearDown() {
    wireMockServer.stop();
  }

  private SubscriberEmail sender() {
    return SubscriberEmail.parse("sender@gmail.com").orElseThrow();
  }

  private SubscriberEmail recipient() {
    return SubscriberEmail.parse("receiver@gmail.com").orElseThrow();
  }

  private RestEmailClient emailClient(String baseUrl) {
    return new RestEmailClient(baseUrl, sender(), "test-token", Duration.ofMillis(200));
  }

  private String subject() {
    return "hello";
  }

  private String htmlContent() {
    return "<p>hello</p>";
  }

  private String textContent() {
    return "hello";
  }

  @Test
  void sendEmailSendsTheExpectedRequest() {
    wireMockServer.stubFor(
        post(urlEqualTo("/email"))
            .withHeader("X-Postmark-Server-Token", equalTo("test-token"))
            .withHeader("Content-Type", containing("application/json"))
            .withRequestBody(matchingJsonPath("$.From"))
            .withRequestBody(matchingJsonPath("$.To"))
            .withRequestBody(matchingJsonPath("$.Subject"))
            .withRequestBody(matchingJsonPath("$.HtmlBody"))
            .withRequestBody(matchingJsonPath("$.TextBody"))
            .willReturn(aResponse().withStatus(200)));

    RestEmailClient client = emailClient(wireMockServer.baseUrl());

    assertThatCode(() -> client.sendEmail(recipient(), subject(), htmlContent(), textContent()))
        .doesNotThrowAnyException();

    wireMockServer.verify(
        1,
        postRequestedFor(urlEqualTo("/email"))
            .withHeader("X-Postmark-Server-Token", equalTo("test-token"))
            .withHeader("Content-Type", containing("application/json")));
  }

  @Test
  void sendEmailUsesPascalCaseJsonFields() {
    wireMockServer.stubFor(post(urlEqualTo("/email")).willReturn(aResponse().withStatus(200)));

    RestEmailClient client = emailClient(wireMockServer.baseUrl());

    client.sendEmail(recipient(), subject(), htmlContent(), textContent());

    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/email"))
            .withRequestBody(
                equalToJson(
                    """
                                                {
                                                  "From": "sender@gmail.com",
                                                  "To": "receiver@gmail.com",
                                                  "Subject": "hello",
                                                  "HtmlBody": "<p>hello</p>",
                                                  "TextBody": "hello"
                                                }
                                                """)));
  }

  @Test
  void sendEmailSucceedsIfTheServerReturns200() {
    wireMockServer.stubFor(post(urlEqualTo("/email")).willReturn(aResponse().withStatus(200)));

    RestEmailClient client = emailClient(wireMockServer.baseUrl());

    assertThatCode(() -> client.sendEmail(recipient(), subject(), htmlContent(), textContent()))
        .doesNotThrowAnyException();
  }

  @Test
  void sendEmailFailsIfTheServerReturns500() {
    wireMockServer.stubFor(
        post(urlEqualTo("/email")).willReturn(aResponse().withStatus(500).withBody("boom")));

    RestEmailClient client = emailClient(wireMockServer.baseUrl());

    assertThatThrownBy(() -> client.sendEmail(recipient(), subject(), htmlContent(), textContent()))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Email API returned error");
  }

  @Test
  void sendEmailTimesOutIfServerIsSlow() {
    wireMockServer.stubFor(
        post(urlEqualTo("/email")).willReturn(aResponse().withStatus(200).withFixedDelay(3000)));

    RestEmailClient client = emailClient(wireMockServer.baseUrl());

    assertThatThrownBy(() -> client.sendEmail(recipient(), subject(), htmlContent(), textContent()))
        .isInstanceOf(Exception.class);
  }
}
