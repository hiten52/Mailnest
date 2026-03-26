package com.mailnest.api;

import com.mailnest.subscriptions.Subscriber;
import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

public class TestApiClient {

  private final String baseUrl;
  private final HttpClient client;
  private final SubscriberRepository subscriberRepository;
  private final SubscriptionTokenRepository tokenRepository;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "Spring-managed service dependency is injected via constructor and not exposed.")
  public TestApiClient(
      int port,
      SubscriberRepository subscriberRepository,
      SubscriptionTokenRepository tokenRepository) {

    this.baseUrl = "http://localhost:" + port;
    this.client = HttpClient.newHttpClient();
    this.subscriberRepository = subscriberRepository;
    this.tokenRepository = tokenRepository;
  }

  public HttpResponse<String> getHealthCheck() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(baseUrl + "/health_check")).GET().build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  public HttpResponse<String> postSubscriptions(String body)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/subscriptions"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  public HttpResponse<String> getSubscriptionConfirmation()
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(baseUrl + "/subscriptions/confirm")).GET().build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  public HttpResponse<String> getSubscriptionConfirmation(String token)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/subscriptions/confirm?token=" + token))
            .GET()
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  public void clearSubscribers() {
    tokenRepository.deleteAll();
    subscriberRepository.deleteAll();
  }

  public List<Subscriber> getSavedSubscribers() {
    return subscriberRepository.findAll();
  }

  public String baseUrl() {
    return baseUrl;
  }
}
