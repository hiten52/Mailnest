package com.mailnest.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.mailnest.subscriptions.Subscriber;
import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

public class TestApiClient {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final String baseUrl;
  private final int port;
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
    this.port = port;
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

  public HttpResponse<String> getSubscriptionConfirmation(URI uri)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

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

  public ConfirmationLinks getConfirmationLinks(ServeEvent emailRequest) {
    try {
      JsonNode body = objectMapper.readTree(emailRequest.getRequest().getBodyAsString());

      URI html = getLink(body.get("HtmlBody").asText());
      URI plainText = getLink(body.get("TextBody").asText());

      return new ConfirmationLinks(html, plainText);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to extract confirmation links from email request", e);
    }
  }

  private URI getLink(String text) throws Exception {
    Pattern pattern = Pattern.compile("https?://[^\\s\"'<>]+");
    Matcher matcher = pattern.matcher(text);

    if (!matcher.find()) {
      throw new AssertionError("Expected exactly one link, found none.");
    }

    String rawLink = matcher.group();

    if (matcher.find()) {
      throw new AssertionError("Expected exactly one link, found more than one.");
    }

    URI uri = URI.create(rawLink);

    if (!"localhost".equals(uri.getHost()) && !"127.0.0.1".equals(uri.getHost())) {
      throw new AssertionError("Expected localhost/127.0.0.1 host but got " + uri.getHost());
    }

    return new URI(
        uri.getScheme(),
        uri.getUserInfo(),
        uri.getHost(),
        port,
        uri.getPath(),
        uri.getQuery(),
        uri.getFragment());
  }

  public record ConfirmationLinks(URI html, URI plainText) {}
}
