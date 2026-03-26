package com.mailnest.email;

import com.mailnest.domain.SubscriberEmail;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

public class RestEmailClient implements EmailClient {

  private static final Logger log = LoggerFactory.getLogger(RestEmailClient.class);

  private final WebClient webClient;
  private final SubscriberEmail sender;
  private final String authToken;

  public RestEmailClient(
      String baseUrl, SubscriberEmail sender, String authToken, Duration timeout) {
    HttpClient httpClient = HttpClient.create().responseTimeout(timeout);

    this.webClient =
        WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();

    this.sender = sender;
    this.authToken = authToken;
  }

  @Override
  public void sendEmail(
      SubscriberEmail recipient, String subject, String htmlContent, String textContent) {

    SendEmailRequest request =
        new SendEmailRequest(
            sender.asString(), recipient.asString(), subject, htmlContent, textContent);

    log.info("Sending email to {}", recipient.asString());

    webClient
        .post()
        .uri("/email")
        .header("X-Postmark-Server-Token", authToken)
        .bodyValue(request)
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            response ->
                response
                    .bodyToMono(String.class)
                    .map(body -> new IllegalStateException("Email API returned error: " + body)))
        .toBodilessEntity()
        .block();

    log.info("Email sent successfully");
  }
}
